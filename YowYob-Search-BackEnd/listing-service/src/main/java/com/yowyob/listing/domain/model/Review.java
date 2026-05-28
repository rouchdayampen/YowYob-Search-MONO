package com.yowyob.listing.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Review du domaine.
 * POJO pur — 0 dépendance JPA / Spring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private UUID id;
    private Integer rating;
    private String comment;
    private String userId;
    private UUID listingId;
    private LocalDateTime createdAt;
}
