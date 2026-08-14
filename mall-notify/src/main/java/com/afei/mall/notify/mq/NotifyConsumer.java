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
            record.setStatus(0);
            record.setIsRead(0);
            record.setRetryCount(0);
            record.setCreateTime(LocalDateTime.now());

            notifyRecordMapper.insert(record);

            // 站内信即写即成功（status=1）
            record.setStatus(1);
            record.setSendTime(LocalDateTime.now());
            notifyRecordMapper.updateById(record);

            log.info("站内信已生成: userId={}, title={}, id={}", msg.getUserId(), msg.getTitle(), record.getId());
        } catch (Exception e) {
            log.error("生成站内信失败: userId={}", msg.getUserId(), e);
        }
    }
}