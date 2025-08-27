package com.fitness.aiservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // Injects values from application.yml
    @Value("${rabbitmq.queue.name}")
    private String queue;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    /**
     * Creates a durable queue for storing messages.
     * Durable queues survive broker restarts.
     */
    @Bean
    public Queue activityQueue() {
        return new Queue(queue, true);  // true makes the queue durable
    }

    /**
     * Creates a Direct Exchange.
     * Direct exchanges route messages to queues based on an exact routing key match.
     */
    @Bean
    public DirectExchange activityExchange() {
        return new DirectExchange(exchange);
    }

    /**
     * Binds the queue to the exchange with a specific routing key.
     * This creates the relationship between exchange, queue and routing key.
     *
     * Message Flow:
     * 1. Producer sends message to exchange with routing key
     * 2. Exchange routes message to bound queues based on routing key
     * 3. Queue receives the message
     * 4. Consumer receives message from the queue
     *
     * @param activityQueue The queue to bind
     * @param activityExchange The exchange to bind to
     * @return Binding configuration
     */
    @Bean
    public Binding activityBinding(Queue activityQueue, DirectExchange activityExchange) {
        return BindingBuilder
                .bind(activityQueue)
                .to(activityExchange)
                .with(routingKey);
    }

    /**
     * Configures JSON message converter for RabbitTemplate.
     * Converts Java objects to JSON when sending messages
     * and JSON back to Java objects when receiving.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}