package com.yowyob.listing.adapter.in.event;

import com.yowyob.listing.domain.model.Listing;
import com.yowyob.listing.domain.model.ListingStatus;
import com.yowyob.listing.domain.port.in.ListingUseCase;
import com.yowyob.listing.dto.CrawlerListingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptateur d'entrée événementiel — écoute les messages Kafka du crawler
 * et les traduit en appels au port d'entrée {@link ListingUseCase}.
 * Remplace CrawlerListingKafkaListener (ancienne couche service/).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CrawlerListingKafkaAdapter {

    private final ListingUseCase listingUseCase;

    @KafkaListener(
            topics = "crawler.listings.events",
            groupId = "listing-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onCrawlerListing(
            CrawlerListingRequest request,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received crawler listing from topic={} offset={} externalId={}",
                topic, offset, request.getExternalId());
        try {
            Listing listing = Listing.builder()
                    .title(request.getTitle())
                    .description(request.getDescription() != null
                            ? request.getDescription()
                            : "Source: " + request.getSource())
                    .price(request.getPrice() != null ? request.getPrice() : 0.0)
                    .category(request.getCategory() != null ? request.getCategory() : "GENERAL")
                    .address(request.getCity() != null
                            ? request.getCity() + ", " + request.getCountry()
                            : request.getCountry())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .imageUrl(request.getImageUrl())
                    .phone(request.getPhone())
                    .openingHours(request.getOpeningHours())
                    .rating(request.getRating())
                    .reviewsCount(request.getReviewsCount())
                    .status(ListingStatus.ACTIVE)
                    .externalId(request.getExternalId())
                    .sellerId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                    .build();

            listingUseCase.createListing(listing);
            log.info("Listing saved successfully — externalId={}", request.getExternalId());

        } catch (Exception e) {
            log.error("Failed to process listing externalId={} — reason: {}",
                    request.getExternalId(), e.getMessage(), e);
            throw e; // Relance pour déclencher le DefaultErrorHandler → DLT
        }
    }
}
