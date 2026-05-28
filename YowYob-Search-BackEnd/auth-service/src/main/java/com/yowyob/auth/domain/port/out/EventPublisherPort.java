package com.yowyob.auth.domain.port.out;

import com.yowyob.auth.domain.model.AuthUser;

/**
 * Port de sortie — interface vers la messagerie (Kafka).
 * Implémenté par {@link com.yowyob.auth.adapter.out.messaging.KafkaUserEventAdapter}.
 * Le domaine ne connaît pas Kafka.
 */
public interface EventPublisherPort {

    void publishUserCreated(AuthUser user);
}
