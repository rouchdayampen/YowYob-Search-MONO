package com.yowyob.listing.event; // Mimicking source package for easier deserialization if using standard serializers

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ListingEvent {
    private UUID id;
    private String title;
    private String description;
    private Double price;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String status;
    private UUID sellerId;
    private String eventType; // BRAND_NEW, UPDATE, DELETED
}
