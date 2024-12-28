package com.zhixian.mall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MQConfig {
    

    @Bean
    public Binding orderReleaseOtherBinding() {

        return new Binding(
                "stock.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other",
                null
        );
    }

    @Bean
    public Queue OrderDelayQueue() {

        return new Queue(
                "order.delay.queue",
                true,
                false,
                false,
                Map.of(
                        "x-dead-letter-exchange", "order-event-exchange",
                        "x-dead-letter-routing-key", "order.release",
                        "x-message-ttl", 60000
                )
        );
    }

    @Bean
    public Queue OrderReleaseQueue() {

        return new Queue(
                "order.release.queue",
                true,
                false,
                false
        );
    }

    @Bean
    public Exchange OrderEventExchange() {

        return new TopicExchange(
                "order-event-exchange",
                true,
                false
        );
    }

    @Bean
    public Binding orderCreateBinding() {

        return new Binding(
                "order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create",
                null
        );
    }

    @Bean
    public Binding orderReleaseBinding() {

        return new Binding(
                "order.release.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release",
                null
        );
    }

}
