package com.fundoonotes.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REMINDER_EXCHANGE =
            "fundoo.reminder.exchange";

    public static final String REMINDER_QUEUE =
            "fundoo.reminder.queue";

    public static final String REMINDER_ROUTING_KEY =
            "reminder.due";

    // Durable queue survives a RabbitMQ restart.
    @Bean
    public Queue reminderQueue() {
        return new Queue(REMINDER_QUEUE, true);
    }

    // Direct exchange routes messages using a matching key.
    @Bean
    public DirectExchange reminderExchange() {
        return new DirectExchange(
                REMINDER_EXCHANGE,
                true,
                false
        );
    }

    // Connect the reminder exchange to the reminder queue.
    @Bean
    public Binding reminderBinding(
            Queue reminderQueue,
            DirectExchange reminderExchange
    ) {
        return BindingBuilder
                .bind(reminderQueue)
                .to(reminderExchange)
                .with(REMINDER_ROUTING_KEY);
    }
}
