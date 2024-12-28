package com.zhixian.mall.inventory.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitConfig {

    /**
     * 使用json序列化方式
     *
     * @return MessageConverter
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange(
                "stock-event-exchange",
                true,
                false
        );
    }

    @Bean
    public Queue stockReleaseQueue() {
        return new Queue(
                "stock.release.queue",
                true,
                false,
                false
        );
    }

    @Bean
    public Queue stockDelayQueue() {
        return new Queue(
                "stock.delay.queue",
                true,
                false,
                false,
                Map.of(
                        "x-dead-letter-exchange", "stock-event-exchange",
                        "x-dead-letter-routing-key", "stock.release",
                        "x-message-ttl", 120000
                )
        );
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding(
                "stock.release.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null
        );
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding(
                "stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null
        );
    }
}
