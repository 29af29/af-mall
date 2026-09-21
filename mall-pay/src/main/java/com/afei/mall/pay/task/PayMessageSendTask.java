package com.afei.mall.pay.task;

import com.afei.common.mq.MqConfig;
import com.afei.common.mq.OrderPaidMessage;
import com.afei.mall.pay.domain.po.PayMessage;
import com.afei.mall.pay.mapper.PayMessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付消息可靠发送任务：每 10 秒扫描 pay_message 待发送记录（status=0），投递到 MQ 订单已支付队列
 * 成功置为已发送；失败重试（最多 5 次），超限置为失败待人工处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayMessageSendTask {

    private static final int MAX_RETRY = 5;

    private final PayMessageMapper payMessageMapper;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 10000)
    public void sendPending() {
        List<PayMessage> pending = payMessageMapper.selectList(
                new LambdaQueryWrapper<PayMessage>()
                        .eq(PayMessage::getStatus, 0)
                        .lt(PayMessage::getRetryCount, MAX_RETRY)
                        .last("LIMIT 20"));
        for (PayMessage message : pending) {
            try {
                OrderPaidMessage msg = new OrderPaidMessage(message.getOrderNo(), message.getPayNo());
                rabbitTemplate.convertAndSend(MqConfig.ORDER_PAID_QUEUE, msg);
                message.setStatus(1);
                message.setSendTime(LocalDateTime.now());
                message.setLastError(null);
                log.info("支付成功消息已发送: orderNo={}", message.getOrderNo());
            } catch (Exception e) {
                message.setRetryCount(message.getRetryCount() + 1);
                message.setLastError("发送失败，第" + message.getRetryCount() + "次重试");
                if (message.getRetryCount() >= MAX_RETRY) {
                    message.setStatus(2);
                    log.error("支付成功消息多次发送失败，待人工处理: orderNo={}", message.getOrderNo());
                }
                log.error("发送支付成功消息失败: orderNo={}", message.getOrderNo(), e);
            }
            payMessageMapper.updateById(message);
        }
    }
}
