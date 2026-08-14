package com.afei.mall.order.mq;

import com.afei.common.mq.MqConfig;
import com.afei.common.mq.OrderPaidMessage;
import com.afei.mall.order.domain.dto.StatusSaveDTO;
import com.afei.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = MqConfig.ORDER_PAID_QUEUE)
    public void handle(OrderPaidMessage msg) {
        log.info("收到支付成功消息: orderNo={}", msg.getOrderNo());
        try {
            StatusSaveDTO dto = new StatusSaveDTO();
            dto.setStatus(2); // 已付款
            orderService.updateStatusByOrderNo(msg.getOrderNo(), dto);
            log.info("订单状态更新成功: orderNo={}", msg.getOrderNo());
        } catch (Exception e) {
            log.error("订单状态更新失败: orderNo={}", msg.getOrderNo(), e);
        }
    }
}
