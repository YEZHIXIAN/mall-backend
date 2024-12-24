package com.zhixian.mall.order.config;

import com.rabbitmq.client.Channel;
import com.zhixian.mall.order.entity.OrderEntity;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Map;

@Configuration
public class MQConfig {

    @RabbitListener(queues = "order.release.queue")
    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期订单信息" + orderEntity);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
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
