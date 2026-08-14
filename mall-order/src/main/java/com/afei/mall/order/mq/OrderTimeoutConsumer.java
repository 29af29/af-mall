package com.afei.mall.order.mq;

import com.afei.common.feign.ProductFeignClient;
import com.afei.common.mq.MqConfig;
import com.afei.common.mq.NotifyMessage;
import com.afei.common.mq.OrderTimeoutMessage;
import com.afei.common.result.Result;
import com.afei.mall.order.domain.po.OrderInfo;
import com.afei.mall.order.domain.po.OrderItem;
import com.afei.mall.order.mapper.OrderInfoMapper;
import com.afei.mall.order.mapper.OrderItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单超时消费者：收到延迟消息后检查订单，未支付则自动关单 + 回补库存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    /** 待支付 */
    private static final int STATUS_UNPAID = 1;
    /** 超时关闭 */
    private static final int STATUS_TIMEOUT_CLOSED = 6;

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductFeignClient productFeignClient;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = MqConfig.ORDER_TIMEOUT_QUEUE)
    public void handle(OrderTimeoutMessage msg) {
        log.info("收到订单超时消息: orderId={}, orderNo={}", msg.getOrderId(), msg.getOrderNo());

        OrderInfo order = orderInfoMapper.selectById(msg.getOrderId());
        if (order == null) {
            log.warn("订单不存在，忽略: orderId={}", msg.getOrderId());
            return;
        }
        // 只有「待支付」才关单；已支付/已取消则忽略（幂等）
        if (order.getStatus() != STATUS_UNPAID) {
            log.info("订单已支付或已关闭，忽略: orderNo={}, status={}", order.getOrderNo(), order.getStatus());
            return;
        }

        // 1. 关单
        order.setStatus(STATUS_TIMEOUT_CLOSED);
        order.setCloseTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);
        log.info("订单超时自动关闭: orderNo={}", order.getOrderNo());

        // 2. 回补库存
        restoreStock(order);

        // 3. 站内信通知
        sendNotify(order, "订单超时关闭", "您的订单 " + order.getOrderNo() + " 超时未支付，已自动关闭");
    }

    private void restoreStock(OrderInfo order) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            try {
                Result<Void> result = productFeignClient.restoreStock(item.getSkuId(), Map.of("num", item.getQuantity()));
                if (result != null && result.getCode() == 200) {
                    log.info("回补库存成功: skuId={}, num={}", item.getSkuId(), item.getQuantity());
                } else {
                    log.warn("回补库存失败: skuId={}, msg={}", item.getSkuId(),
                            result != null ? result.getMessage() : "null");
                }
            } catch (Exception e) {
                log.error("回补库存异常: skuId={}", item.getSkuId(), e);
            }
        }
    }

    private void sendNotify(OrderInfo order, String title, String content) {
        try {
            NotifyMessage notify = new NotifyMessage(order.getUserId(), title, content, NotifyMessage.TYPE_SYSTEM, order.getOrderNo());
            rabbitTemplate.convertAndSend(MqConfig.NOTIFY_QUEUE, notify);
        } catch (Exception e) {
            log.error("发送关单通知失败: orderNo={}", order.getOrderNo(), e);
        }
    }
}
