package com.yowyob.search.adapter.in.event;

import com.yowyob.search.config.RabbitMQConfig;
import com.yowyob.search.domain.model.Product;
import com.yowyob.search.domain.port.in.SearchUseCase;
import com.yowyob.search.event.ListingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adaptateur d'écoute des événements RabbitMQ (Listings).
 * Remplace l'ancien {@code ListingEventListener}.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ListingEventAdapter {

    private final SearchUseCase searchUseCase;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleListingEvent(ListingEvent event) {
        log.info("Received Listing Event: {} for Listing ID: {}", event.getEventType(), event.getId());

        if ("DELETED".equals(event.getEventType())) {
            log.info("Skipping delete for now (not implemented in SearchUseCase yet)");
        } else {
            Product product = Product.builder()
                    .id(event.getId().toString())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .price(event.getPrice())
                    .category(event.getCategory())
                    .city(event.getAddress()) // Mapping Address -> City par défaut
                    .serviceType("LISTING")
                    .imageUrl(event.getImageUrl())
                    .rating(event.getRating())
                    .reviewsCount(event.getReviewsCount())
                    .phone(event.getPhone())
                    .openingHours(event.getOpeningHours())
                    .latitude(event.getLatitude())
                    .longitude(event.getLongitude())
                    .build();

            searchUseCase.indexProduct(product).subscribe(
                    result -> log.info("Successfully indexed listing: {}", result.getId()),
                    error -> log.error("Error indexing listing: {}", error.getMessage())
            );
        }
    }
}
