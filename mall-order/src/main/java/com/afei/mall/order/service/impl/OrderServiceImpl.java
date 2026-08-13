package com.afei.mall.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.afei.common.exception.BusinessException;
import com.afei.common.feign.ProductFeignClient;
import com.afei.common.feign.dto.SkuInfoDTO;
import com.afei.common.jwt.JwtUtils;
import com.afei.common.mq.MqConfig;
import com.afei.common.mq.NotifyMessage;
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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final JwtUtils jwtUtils;
    private final ProductFeignClient productFeignClient;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @GlobalTransactional(name = "createOrder", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(String token, OrderCreateDTO dto) {
        Long userId = jwtUtils.getUserId(token);

        // 1. 存 order_info（先落库拿 orderId）
        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setTotalAmount(dto.getTotalAmount());
        order.setPayAmount(dto.getPayAmount());
        order.setFreightAmount(0L);
        order.setStatus(1);
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setRemark(dto.getRemark());
        orderInfoMapper.insert(order);

        // 2. 扣库存 + 存 order_item 快照
        for (OrderCreateDTO.OrderItemDTO item : dto.getOrderItems()) {
            SkuInfoDTO sku = getSkuInfo(item.getSkuId());
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
    public PageResult<OrderPageVO> orderPage(String token, OrderPageQueryDTO query) {
        Long userId = jwtUtils.getUserId(token);

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
    public OrderDetailVO orderDetail(String token, Long id) {
        Long userId = jwtUtils.getUserId(token);
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
    public void cancelOrder(String token, Long id) {
        Long userId = jwtUtils.getUserId(token);
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权取消该订单");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("仅待付款订单可取消");
        }
        order.setStatus(5);
        order.setCloseTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);
    }

    @Override
    public void updateStatus(Long id, StatusSaveDTO dto) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(dto.getStatus());
        if (dto.getStatus() == 2) {
            order.setPaymentTime(LocalDateTime.now());
            order.setPayType(1);
            // 支付成功通知
            sendNotify(order.getUserId(), "支付成功", "您的订单 " + order.getOrderNo() + " 已支付成功", NotifyMessage.TYPE_PAY, order.getOrderNo());
        }
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

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
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
