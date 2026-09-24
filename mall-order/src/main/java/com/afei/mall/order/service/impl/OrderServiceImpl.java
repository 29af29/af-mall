package com.afei.mall.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.afei.common.exception.BusinessException;
import com.afei.common.feign.ProductFeignClient;
import com.afei.common.feign.dto.SkuInfoDTO;
import com.afei.common.mq.MqConfig;
import com.afei.common.mq.NotifyMessage;
import com.afei.common.mq.OrderTimeoutMessage;
import com.afei.common.result.PageResult;
import com.afei.common.result.Result;
import com.afei.mall.order.domain.dto.OrderCreateDTO;
import com.afei.mall.order.domain.dto.OrderPageQueryDTO;
import com.afei.mall.order.domain.dto.StatusSaveDTO;
import com.afei.mall.order.domain.po.OrderInfo;
import com.afei.mall.order.domain.po.OrderItem;
import com.afei.mall.order.domain.vo.OrderCreateVO;
import com.afei.mall.order.domain.vo.OrderDetailVO;
import com.afei.mall.order.domain.vo.OrderItemVO;
import com.afei.mall.order.domain.vo.OrderPageVO;
import com.afei.mall.order.mapper.OrderInfoMapper;
import com.afei.mall.order.mapper.OrderItemMapper;
import com.afei.mall.order.service.OrderService;
import com.afei.mall.order.service.StockRestoreService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    /** 下单幂等键前缀 */
    private static final String ORDER_IDEMPOTENT_KEY = "order:idem:";

    /** 下单幂等窗口（秒）：窗口内「同一用户 + 同一批商品」视为重复提交 */
    private static final long IDEMPOTENT_EXPIRE_SECONDS = 10;

    private final ProductFeignClient productFeignClient;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final RabbitTemplate rabbitTemplate;
    private final StockRestoreService stockRestoreService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @GlobalTransactional(name = "createOrder", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(Long userId, OrderCreateDTO dto) {
        // 幂等前置拦截：防用户连点 / 网络重试导致的重复下单
        // （order_no 每次请求现生成，唯一索引拦不住同一笔业务的重复提交）
        String idemKey = buildIdempotentKey(userId, dto);
        if (!tryLockIdempotent(idemKey)) {
            log.warn("重复提交下单请求已拦截: userId={}, key={}", userId, idemKey);
            throw new BusinessException("订单提交中，请勿重复提交");
        }
        try {
            // 事务注解在入口方法 createOrder 上，这里抽私有方法不影响事务生效
            return doCreateOrder(userId, dto);
        } catch (Exception e) {
            // 业务失败立即释放幂等键，否则用户重试会被自己上一次的失败挡住
            stringRedisTemplate.delete(idemKey);
            throw e;
        }
    }

    private OrderCreateVO doCreateOrder(Long userId, OrderCreateDTO dto) {
        // 1. 存 order_info（先落库拿 orderId）
        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        // 金额以服务端为准：拉取 SKU 并按 单价 x 数量 重算，不信任前端传入的 totalAmount/payAmount
        Map<Long, SkuInfoDTO> skuMap = new HashMap<>();
        long totalAmount = 0;
        for (OrderCreateDTO.OrderItemDTO item : dto.getOrderItems()) {
            SkuInfoDTO sku = getSkuInfo(item.getSkuId());
            skuMap.put(item.getSkuId(), sku);
            totalAmount += sku.getPrice() * item.getNum();
        }
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setFreightAmount(0L);
        order.setStatus(1);
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setRemark(dto.getRemark());
        orderInfoMapper.insert(order);

        // 2. 扣库存 + 存 order_item 快照
        for (OrderCreateDTO.OrderItemDTO item : dto.getOrderItems()) {
            SkuInfoDTO sku = skuMap.get(item.getSkuId());
            deductStock(item.getSkuId(), item.getNum());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(order.getOrderNo());
            orderItem.setSkuId(item.getSkuId());
            orderItem.setSpuId(sku.getSpuId());
            orderItem.setSkuName(sku.getTitle());
            orderItem.setSkuPic(sku.getImages());
            orderItem.setPrice(sku.getPrice());
            orderItem.setQuantity(item.getNum());
            orderItem.setTotalAmount(sku.getPrice() * item.getNum());
            orderItem.setCreateTime(LocalDateTime.now());
            orderItemMapper.insert(orderItem);
        }

        // 3. 下单成功通知
        sendNotify(userId, "下单成功", "您的订单 " + order.getOrderNo() + " 已创建，请尽快支付", NotifyMessage.TYPE_ORDER, order.getOrderNo());

        // 4. 发送订单超时延迟消息（30 分钟后未支付自动关单）
        sendOrderTimeout(order.getId(), order.getOrderNo());

        return OrderCreateVO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .totalPay(order.getTotalAmount())
                .actualPay(order.getPayAmount())
                .status(order.getStatus())
                .createTime(order.getCreateTime())
                .build();
    }

    @Override
    public PageResult<OrderPageVO> orderPage(Long userId, OrderPageQueryDTO query) {
        Page<OrderInfo> page = orderInfoMapper.selectPage(query.toPage(),
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getUserId, userId)
                        .eq(query.getStatus() != null, OrderInfo::getStatus, query.getStatus())
                        .orderByDesc(OrderInfo::getCreateTime));

        if (page.getRecords().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L, 1, (int) page.getSize());
        }

        // 批量查所有订单的首条 orderItem
        List<Long> orderIds = page.getRecords().stream().map(OrderInfo::getId).toList();
        List<OrderItem> items = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery().in(OrderItem::getOrderId, orderIds));
        Map<Long, OrderItem> itemMap = items.stream()
                .collect(Collectors.toMap(OrderItem::getOrderId, i -> i, (a, b) -> a));

        // 批量查所有订单的商品总数
        Map<Long, Integer> countMap = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery()
                        .in(OrderItem::getOrderId, orderIds)
                        .select(OrderItem::getOrderId, OrderItem::getQuantity))
                .stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId,
                        Collectors.summingInt(OrderItem::getQuantity)));

        List<OrderPageVO> voList = page.getRecords().stream().map(order -> {
            OrderItem item = itemMap.get(order.getId());
            return OrderPageVO.builder()
                    .id(order.getId())
                    .orderNo(order.getOrderNo())
                    .totalAmount(order.getTotalAmount())
                    .payAmount(order.getPayAmount())
                    .status(order.getStatus())
                    .goodsTitle(item != null ? item.getSkuName() : "")
                    .goodsPic(item != null ? item.getSkuPic() : "")
                    .goodsNum(countMap.getOrDefault(order.getId(), 0))
                    .createTime(order.getCreateTime())
                    .build();
        }).collect(Collectors.toList());

        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public OrderDetailVO orderDetail(Long userId, Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }

        OrderDetailVO vo = new OrderDetailVO();
        BeanUtil.copyProperties(order, vo);

        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery().eq(OrderItem::getOrderId, id));
        vo.setOrderItems(orderItems.stream().map(item -> OrderItemVO.builder()
                .skuId(item.getSkuId())
                .title(item.getSkuName())
                .image(item.getSkuPic())
                .price(item.getPrice())
                .num(item.getQuantity())
                .build()).collect(Collectors.toList()));

        return vo;
    }

    @Override
    public void cancelOrder(Long userId, Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权取消该订单");
        }
        // 条件更新：仅待付款(1)可取消，防止与超时关单并发导致重复回补库存
        boolean canceled = orderInfoMapper.update(null,
                new LambdaUpdateWrapper<OrderInfo>()
                        .eq(OrderInfo::getId, id)
                        .eq(OrderInfo::getStatus, 1)
                        .set(OrderInfo::getStatus, 5)
                        .set(OrderInfo::getCloseTime, LocalDateTime.now())) > 0;
        if (!canceled) {
            throw new BusinessException("订单状态已变化，仅待付款订单可取消");
        }
        // 回补库存（失败自动登记重试，由定时任务补偿）
        order.setStatus(5);
        stockRestoreService.restore(order);
    }

    @Override
    public void updateStatus(Long id, StatusSaveDTO dto) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        Integer target = dto.getStatus();
        if (target != null && target == 2) {
            // 支付完成状态机：仅待付款(1)可转已付款(2)，重复消息/重复回调直接忽略（幂等）
            boolean updated = orderInfoMapper.update(null,
                    new LambdaUpdateWrapper<OrderInfo>()
                            .eq(OrderInfo::getId, id)
                            .eq(OrderInfo::getStatus, 1)
                            .set(OrderInfo::getStatus, 2)
                            .set(OrderInfo::getPaymentTime, LocalDateTime.now())
                            .set(OrderInfo::getPayType, 1)) > 0;
            if (!updated) {
                log.warn("订单状态不允许支付完成，忽略: orderNo={}, status={}", order.getOrderNo(), order.getStatus());
                return;
            }
            // 支付成功通知
            sendNotify(order.getUserId(), "支付成功", "您的订单 " + order.getOrderNo() + " 已支付成功", NotifyMessage.TYPE_PAY, order.getOrderNo());
            return;
        }
        // 其余状态（管理端手动调整）
        order.setStatus(target);
        orderInfoMapper.updateById(order);
    }

    @Override
    public void updateStatusByOrderNo(String orderNo, StatusSaveDTO dto) {
        OrderInfo order = orderInfoMapper.selectOne(
                Wrappers.<OrderInfo>lambdaQuery().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        updateStatus(order.getId(), dto);
    }

    /**
     * 订单超时关闭（幂等）：MQ 延迟消息 / 定时任务兜底均可调用
     * <p>
     * 1. 仅 status=1 的订单会被关闭（条件更新 CAS）
     * 2. 关闭后调用 stockRestoreService 回补库存（内部有重复回补防护）
     * 3. 发站内信通知
     * <p>
     * 这是 OrderTimeoutConsumer 关单逻辑的 Service 层版本，Consumer 和定时任务复用同一份逻辑
     */
    @Override
    public void timeoutClose(Long orderId) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在，忽略超时关单: orderId={}", orderId);
            return;
        }
        if (order.getStatus() != 1) {
            log.info("订单已支付或已关闭，忽略超时关单: orderNo={}, status={}", order.getOrderNo(), order.getStatus());
            return;
        }
        boolean closed = orderInfoMapper.update(null,
                new LambdaUpdateWrapper<OrderInfo>()
                        .eq(OrderInfo::getId, orderId)
                        .eq(OrderInfo::getStatus, 1)
                        .set(OrderInfo::getStatus, 5)
                        .set(OrderInfo::getCloseTime, LocalDateTime.now())) > 0;
        if (!closed) {
            log.info("订单状态已变化，并发跳过: orderNo={}", order.getOrderNo());
            return;
        }
        log.info("订单超时自动关闭: orderNo={}", order.getOrderNo());
        stockRestoreService.restore(order);
        sendNotify(order.getUserId(), "订单超时关闭", "您的订单 " + order.getOrderNo() + " 超时未支付，已自动关闭", NotifyMessage.TYPE_SYSTEM, order.getOrderNo());
    }

    /**
     * 发送站内信通知（容错：MQ 故障不影响主业务）
     */
    private void sendNotify(Long userId, String title, String content, Integer type, String businessId) {
        try {
            NotifyMessage msg = new NotifyMessage(userId, title, content, type, businessId);
            rabbitTemplate.convertAndSend(MqConfig.NOTIFY_QUEUE, msg);
        } catch (Exception e) {
            log.error("发送站内信通知失败: userId={}, title={}", userId, title, e);
        }
    }

    /**
     * 发送订单超时延迟消息（未支付自动关单）
     */
    private void sendOrderTimeout(Long orderId, String orderNo) {
        try {
            OrderTimeoutMessage msg = new OrderTimeoutMessage(orderId, orderNo);
            rabbitTemplate.convertAndSend(
                    MqConfig.ORDER_TIMEOUT_EXCHANGE,
                    MqConfig.ORDER_TIMEOUT_ROUTING_KEY,
                    msg,
                    m -> {
                        // 30 分钟（生产），测试可改 30 * 1000
                        m.getMessageProperties().setHeader("x-delay", 1800000);
                        return m;
                    });
            log.info("订单超时消息已发送: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("发送订单超时消息失败: orderNo={}", orderNo, e);
        }
    }

    /**
     * 下单幂等键：同一用户 + 同一批商品（SKU 与数量一致，顺序无关）在窗口内视为同一笔下单
     * <p>
     * 不依赖前端传 requestId，服务端自行推导，Postman 直接调也能生效
     */
    private String buildIdempotentKey(Long userId, OrderCreateDTO dto) {
        List<OrderCreateDTO.OrderItemDTO> items = dto.getOrderItems();
        if (items == null || items.isEmpty()) {
            throw new BusinessException("订单商品不能为空");
        }
        String fingerprint = items.stream()
                .sorted(Comparator.comparing(OrderCreateDTO.OrderItemDTO::getSkuId,
                        Comparator.nullsLast(Comparator.<Long>naturalOrder())))
                .map(item -> item.getSkuId() + "x" + item.getNum())
                .collect(Collectors.joining(","));
        return ORDER_IDEMPOTENT_KEY + userId + ":" + fingerprint;
    }

    /**
     * SETNX 抢占幂等键，抢占失败说明窗口内已有同笔请求在处理
     * <p>
     * Redis 异常时降级放行（可用性优先）：下单不该因为缓存故障整体不可用，
     * 最后一道防线由 order_info.uk_order_no 唯一索引兜底
     */
    private boolean tryLockIdempotent(String idemKey) {
        try {
            Boolean first = stringRedisTemplate.opsForValue()
                    .setIfAbsent(idemKey, "1", Duration.ofSeconds(IDEMPOTENT_EXPIRE_SECONDS));
            return Boolean.TRUE.equals(first);
        } catch (Exception e) {
            log.error("下单幂等校验异常，降级放行: key={}", idemKey, e);
            return true;
        }
    }

    /**
     * 订单号：雪花算法（19 位数字），保证分布式下全局唯一，配合 order_info.uk_order_no 唯一索引兜底
     */
    private String generateOrderNo() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    private SkuInfoDTO getSkuInfo(Long skuId) {
        Result<SkuInfoDTO> result = productFeignClient.skuDetail(skuId);
        if (result == null || result.getCode() != 200) {
            throw new BusinessException("商品不存在");
        }
        return result.getData();
    }

    private void deductStock(Long skuId, Integer num) {
        try {
            Result<Void> result = productFeignClient.deductStock(skuId, Map.of("num", num));
            if (result != null && result.getCode() != 200) {
                throw new BusinessException(result.getMessage());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("扣库存失败");
        }
    }
}
