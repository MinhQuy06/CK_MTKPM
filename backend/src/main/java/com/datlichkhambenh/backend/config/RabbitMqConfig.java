package com.datlichkhambenh.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    Queue emailQueue(@Value("${app.messaging.email-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    DirectExchange emailExchange(@Value("${app.messaging.email-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Binding emailBinding(Queue emailQueue, DirectExchange emailExchange,
                         @Value("${app.messaging.email-routing-key}") String routingKey) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(routingKey);
    }

    @Bean
    Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                  Jackson2JsonMessageConverter rabbitMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(rabbitMessageConverter);
        return template;
    }
}
