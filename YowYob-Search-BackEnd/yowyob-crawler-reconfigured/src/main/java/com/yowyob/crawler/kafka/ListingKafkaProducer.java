package com.yowyob.crawler.kafka;

import com.yowyob.crawler.dto.ListingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ListingKafkaProducer {

    @Value("${kafka.topics.listings:listings.raw}")
    private String topic;

    @Autowired(required = false)
    private KafkaTemplate<String, ListingEvent> kafkaTemplate;

    public void publish(ListingEvent event) {
        if (kafkaTemplate == null) {
            log.warn("[MODE LIGHT] Kafka non disponible — listing non publié : {}", event.getName());
            return;
        }
        kafkaTemplate.send(topic, event.getOsmId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Échec Kafka pour {} : {}",
                        event.getName(), ex.getMessage());
                } else {
                    log.debug("Publié : {}", event.getName());
                }
            });
    }

    public void publishAll(List<ListingEvent> events) {
        events.forEach(this::publish);
        log.info("{} événements traités vers '{}'", events.size(), topic);
    }
}
