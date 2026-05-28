package com.yowyob.listing.adapter.out.messaging;

import com.yowyob.listing.config.RabbitMQConfig;
import com.yowyob.listing.domain.model.Listing;
import com.yowyob.listing.domain.port.out.ListingEventPublisher;
import com.yowyob.listing.event.ListingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptateur de sortie — implémente le port {@link ListingEventPublisher} du domaine
 * en publiant des messages via RabbitMQ.
 * Le domaine ne connaît pas RabbitMQ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQListingPublisher implements ListingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishCreated(Listing listing) {
        publish(listing, "CREATED");
    }

    @Override
    public void publishUpdated(Listing listing) {
        publish(listing, "UPDATED");
    }

    @Override
    public void publishDeleted(Listing listing) {
        publish(listing, "DELETED");
    }

    private void publish(Listing listing, String eventType) {
        ListingEvent event = ListingEvent.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .price(listing.getPrice())
                .category(listing.getCategory())
                .address(listing.getAddress())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .imageUrl(listing.getImageUrl())
                .phone(listing.getPhone())
                .openingHours(listing.getOpeningHours())
                .rating(listing.getRating())
                .reviewsCount(listing.getReviewsCount())
                .status(listing.getStatus() != null ? listing.getStatus().name() : null)
                .sellerId(listing.getSellerId())
                .eventType(eventType)
                .build();

        String routingKey = "listing." + eventType.toLowerCase();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
        log.info("Published {} event for listing id={}", eventType, listing.getId());
    }
}
