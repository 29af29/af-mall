package com.afei.mall.order.mq;

import com.afei.common.mq.MqConfig;
import com.afei.common.mq.OrderTimeoutMessage;
import com.afei.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时消费者：MQ 延迟消息触发，调用 service.timeoutClose 关闭订单
 * <p>
 * 注意：核心关单逻辑已下沉到 OrderServiceImpl.timeoutClose，
 * MQ 消费者和定时任务（OrderTimeoutTask）共用同一份逻辑，保证行为一致。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = MqConfig.ORDER_TIMEOUT_QUEUE)
    public void handle(OrderTimeoutMessage msg) {
        log.info("收到订单超时消息: orderId={}, orderNo={}", msg.getOrderId(), msg.getOrderNo());
        try {
            orderService.timeoutClose(msg.getOrderId());
        } catch (Exception e) {
            log.error("订单超时关闭异常: orderId={}", msg.getOrderId(), e);
            // 抛出异常让 MQ 重试（也可选择不抛，由定时任务兜底）
            throw e;
        }
    }
}