package com.yowyob.auth.adapter.out.messaging;

import com.yowyob.auth.domain.model.AuthUser;
import com.yowyob.auth.domain.port.out.EventPublisherPort;
import com.yowyob.auth.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Adaptateur de messagerie implémentant {@link EventPublisherPort}.
 * Encapsule Kafka — le domaine ne connaît pas KafkaTemplate.
 */
@Component
@Slf4j
public class KafkaUserEventAdapter implements EventPublisherPort {

    @Autowired(required = false)
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    private static final String TOPIC = "user.events";

    @Override
    @Async
    public void publishUserCreated(AuthUser user) {
        if (kafkaTemplate == null) {
            log.warn("Kafka désactivé (mode light) — événement non envoyé pour: {}", user.getEmail());
            return;
        }

        UserCreatedEvent event = UserCreatedEvent.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .username(user.getName())
                .firstName(user.getName())
                .build();

        log.info("Sending UserCreatedEvent to Kafka: {}", event);
        try {
            kafkaTemplate.send(TOPIC, event.getId(), event);
            log.info("Successfully sent event for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send event to Kafka: {}", e.getMessage(), e);
        }
    }
}
