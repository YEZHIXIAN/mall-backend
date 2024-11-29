package com.zhixian.mall.order.config;

import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    /**
     * 使用json序列化方式
     * @return MessageConverter
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 设置消息发送确认和回退
     */
    public RabbitTemplate rabbitTemplate(RabbitTemplate rabbitTemplate) {
        rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
            if (ack) {
                // Handle success
                System.out.println("Message sent successfully: " + correlationData);
            } else {
                // Handle failure
                System.out.println("Message failed to send: " + cause);
            }
        });

        rabbitTemplate.setReturnsCallback((ReturnedMessage returnedMessage) -> {
            System.out.println("Message lost: " + returnedMessage);
        });

        return rabbitTemplate;
    }
}
