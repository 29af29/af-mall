package com.afei.common.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MqConfig {

    public static final String ORDER_PAID_QUEUE = "order.paid.queue";
    public static final String SPU_SYNC_QUEUE = "spu.sync.queue";
    public static final String NOTIFY_QUEUE = "notify.queue";

    // 订单超时（延迟交换机）
    public static final String ORDER_TIMEOUT_EXCHANGE = "order.timeout.exchange";
    public static final String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

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

    /**
     * 订单超时延迟交换机（依赖 rabbitmq_delayed_message_exchange 插件）
     */
    @Bean
    public CustomExchange orderTimeoutExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(ORDER_TIMEOUT_EXCHANGE, "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return new Queue(ORDER_TIMEOUT_QUEUE, true);
    }

    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(orderTimeoutExchange())
                .with(ORDER_TIMEOUT_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
