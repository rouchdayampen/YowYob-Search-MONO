package com.yowyob.listing.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Agrégat racine du domaine Listing.
 * POJO pur — 0 dépendance JPA / Spring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    private UUID id;
    private String externalId;
    private String title;
    private String description;
    private Double price;
    private String category;
    private UUID sellerId;
    private String address;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String phone;
    private String openingHours;
    private Double averageRating;
    private Integer reviewCount;
    private String osmId;
    private Double rating;
    private Integer reviewsCount;
    private ListingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}
