package com.yowyob.crawler.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "listing.events";
    public static final String QUEUE_NAME = "crawler-listing-queue";
    public static final String ROUTING_KEY = "listing.#";

    @Bean
    public TopicExchange listingExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue crawlerQueue() {
        return new Queue(QUEUE_NAME, true); // Durable queue
    }

    @Bean
    public Binding binding(Queue crawlerQueue, TopicExchange listingExchange) {
        return BindingBuilder.bind(crawlerQueue).to(listingExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
