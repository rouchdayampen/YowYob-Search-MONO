package com.yowyob.crawler.rabbitmq;

import com.yowyob.crawler.dto.ListingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Publie les événements de listing sur RabbitMQ (exchange: listing.events).
 * Le search-service écoute cette exchange et indexe dans Elasticsearch.
 */
@Service
@Slf4j
public class ListingRabbitMQPublisher {

    private static final String EXCHANGE = "listing.events";
    private static final String ROUTING_KEY = "listing.created";

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    public void publishAll(List<ListingEvent> events) {
        if (rabbitTemplate == null) {
            log.warn("[MODE LIGHT] RabbitTemplate non disponible — {} événements non publiés", events.size());
            return;
        }
        int count = 0;
        for (ListingEvent event : events) {
            try {
                // Convertit en format attendu par le search-service
                SearchListingEvent searchEvent = toSearchEvent(event);
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, searchEvent);
                count++;
            } catch (Exception e) {
                log.error("Erreur publication RabbitMQ pour '{}' : {}", event.getName(), e.getMessage());
            }
        }
        log.info("✅ {} événements publiés sur RabbitMQ (exchange: {})", count, EXCHANGE);
    }

    private SearchListingEvent toSearchEvent(ListingEvent event) {
        SearchListingEvent se = new SearchListingEvent();
        se.setId(UUID.randomUUID());
        se.setTitle(event.getName());
        se.setDescription(buildDescription(event));
        se.setCategory(event.getCategory());
        se.setAddress(event.getAddress());
        se.setStreet(event.getStreet());
        se.setLatitude(event.getLatitude());
        se.setLongitude(event.getLongitude());
        se.setImageUrl(event.getImageUrl());
        se.setPhone(event.getPhone());
        se.setOpeningHours(event.getOpeningHours());
        se.setStatus("ACTIVE");
        se.setEventType("CREATED");
        se.setPrice(0.0);
        return se;
    }

    private String buildDescription(ListingEvent event) {
        StringBuilder sb = new StringBuilder();
        if (event.getCategory() != null) sb.append(event.getCategory()).append(" ");
        if (event.getSourceCity() != null) sb.append("à ").append(event.getSourceCity()).append(". ");
        if (event.getWebsite() != null) sb.append("Site: ").append(event.getWebsite());
        return sb.toString().trim();
    }

    // DTO compatible avec le search-service
    public static class SearchListingEvent {
        private UUID id;
        private String title;
        private String description;
        private Double price;
        private String category;
        private String address;
        private String street;
        private Double latitude;
        private Double longitude;
        private String status;
        private UUID sellerId;
        private String eventType;
        private String imageUrl;
        private String phone;
        private String openingHours;
        private Double rating;
        private Integer reviewsCount;

        // Getters & Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public UUID getSellerId() { return sellerId; }
        public void setSellerId(UUID sellerId) { this.sellerId = sellerId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getOpeningHours() { return openingHours; }
        public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        public Integer getReviewsCount() { return reviewsCount; }
        public void setReviewsCount(Integer reviewsCount) { this.reviewsCount = reviewsCount; }
    }
}
