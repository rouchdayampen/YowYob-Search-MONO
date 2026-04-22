package com.yowyob.crawler.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ListingServiceKafkaClient {

    private static final String TOPIC = "crawler.listings.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ListingServiceKafkaClient(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendListing(ScrapedListing listing) {
        kafkaTemplate.send(TOPIC, listing.getExternalId(), listing)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish listing externalId={} — reason: {}",
                                listing.getExternalId(), ex.getMessage(), ex);
                    } else {
                        log.info("Published listing externalId={} → topic={} partition={} offset={}",
                                listing.getExternalId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
