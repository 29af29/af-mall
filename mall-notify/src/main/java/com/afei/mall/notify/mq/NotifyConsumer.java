package com.afei.mall.notify.mq;

import com.afei.common.mq.MqConfig;
import com.afei.common.mq.NotifyMessage;
import com.afei.mall.notify.domain.po.NotifyRecord;
import com.afei.mall.notify.mapper.NotifyRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 站内信消费者：MQ 消息到达后落库为「已发送」
 * <p>
 * 设计说明：站内信 = 写入本服务数据库，是一次本地事务，天然原子，不存在"投递失败"环节，
 * 因此不需要本地消息表 + 定时补偿。可靠投递模式只用于跨网络、可能失败的投递场景：
 * 支付成功消息投递 MQ 见 PayMessage + PayMessageSendTask；库存回补见 OrderStockRetry + StockRestoreRetryTask。
 * <p>
 * 若后续接入短信(type=1) / 邮件(type=2) 等外部渠道，可复用 notify_record 的
 * status(0=待发送 / 1=已发送 / 2=失败) 与 retry_count 字段，平滑改造成本地消息表 + 定时任务补偿。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyConsumer {

    /** 站内信类型（对应 notify_record.type） */
    private static final int CHANNEL_IN_APP = 3;

    private final NotifyRecordMapper notifyRecordMapper;

    @RabbitListener(queues = MqConfig.NOTIFY_QUEUE)
    public void handle(NotifyMessage msg) {
        log.info("收到通知消息: userId={}, title={}", msg.getUserId(), msg.getTitle());
        try {
            NotifyRecord record = new NotifyRecord();
            record.setType(CHANNEL_IN_APP);
            record.setTarget(String.valueOf(msg.getUserId()));
            record.setTitle(msg.getTitle());
            record.setContent(msg.getContent());
            record.setBusinessType(String.valueOf(msg.getType()));
            record.setBusinessId(msg.getBusinessId());
            record.setIsRead(0);
            record.setRetryCount(0);
            // 站内信落库即已送达：本地写库天然原子，无需"待发送 -> 投递 -> 已发送"的可靠投递流程
            record.setStatus(1);
            record.setCreateTime(LocalDateTime.now());
            record.setSendTime(LocalDateTime.now());
            notifyRecordMapper.insert(record);

            log.info("站内信已生成: userId={}, title={}, id={}", msg.getUserId(), msg.getTitle(), record.getId());
        } catch (Exception e) {
            log.error("生成站内信失败: userId={}", msg.getUserId(), e);
        }
    }
}