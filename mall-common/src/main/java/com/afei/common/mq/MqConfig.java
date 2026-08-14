package com.afei.common.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    public static final String ORDER_PAID_QUEUE = "order.paid.queue";
    public static final String SPU_SYNC_QUEUE = "spu.sync.queue";
    public static final String NOTIFY_QUEUE = "notify.queue";

    @Bean
    public Queue orderPaidQueue() {
        return new Queue(ORDER_PAID_QUEUE, true);
    }

    @Bean
    public Queue spuSyncQueue() {
        return new Queue(SPU_SYNC_QUEUE, true);
    }

    @Bean
    public Queue notifyQueue() {
        return new Queue(NOTIFY_QUEUE, true);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
