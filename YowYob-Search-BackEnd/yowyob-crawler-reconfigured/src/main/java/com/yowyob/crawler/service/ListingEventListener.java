package com.yowyob.crawler.service;

import com.yowyob.crawler.config.RabbitMQConfig;
import com.yowyob.crawler.model.ServiceDocument;
import com.yowyob.listing.event.ListingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ListingEventListener {

    private static final Logger log = LoggerFactory.getLogger(ListingEventListener.class);

    private final IndexerService indexerService;

    @Autowired
    public ListingEventListener(IndexerService indexerService) {
        this.indexerService = indexerService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleListingEvent(ListingEvent event) {
        log.info("Received Listing Event: {} for ID: {}", event.getEventType(), event.getId());

        String documentId = "listing_" + event.getId();

        if ("DELETED".equalsIgnoreCase(event.getEventType())) {
            log.info("Processing DELETE for listing: {}", documentId);
            indexerService.delete(documentId);
        } else {
            // CREATED or UPDATED
            log.info("Processing INDEX/UPDATE for listing: {}", documentId);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sellerId", event.getSellerId());
            if (event.getStatus() != null) {
                metadata.put("status", event.getStatus());
            }
            if (event.getLatitude() != null) {
                metadata.put("latitude", event.getLatitude());
            }
            if (event.getLongitude() != null) {
                metadata.put("longitude", event.getLongitude());
            }

            ServiceDocument doc = ServiceDocument.builder()
                    .id(documentId) // Explicitly setting ID here as IndexerService uses it
                    .serviceType("listing")
                    .serviceId(documentId)
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .price(event.getPrice())
                    .category(event.getCategory())
                    .location(event.getAddress())
                    .indexedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now()) // Event doesn't have updatedAt, using now
                    .metadata(metadata)
                    .build();

            indexerService.index(doc);
        }
    }
}
