package com.yowyob.listing.adapter.out.persistence;

import com.yowyob.listing.domain.model.Listing;
import com.yowyob.listing.domain.model.ListingStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Mapper entre le modèle de domaine {@link Listing} et l'entité JPA {@link ListingJpaEntity}.
 * Le domaine ne connaît pas JPA, ce mapper assure la translation.
 */
@Component
public class ListingMapper {

    public Listing toDomain(ListingJpaEntity entity) {
        if (entity == null) return null;
        return Listing.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .category(entity.getCategory())
                .sellerId(entity.getSellerId())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .imageUrl(entity.getImageUrl())
                .phone(entity.getPhone())
                .openingHours(entity.getOpeningHours())
                .averageRating(entity.getAverageRating())
                .reviewCount(entity.getReviewCount())
                .osmId(entity.getOsmId())
                .rating(entity.getRating())
                .reviewsCount(entity.getReviewsCount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .reviews(Collections.emptyList()) // lazy — non chargé ici
                .build();
    }

    public ListingJpaEntity toEntity(Listing domain) {
        if (domain == null) return null;
        return ListingJpaEntity.builder()
                .id(domain.getId())
                .externalId(domain.getExternalId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .category(domain.getCategory())
                .sellerId(domain.getSellerId())
                .address(domain.getAddress())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .imageUrl(domain.getImageUrl())
                .phone(domain.getPhone())
                .openingHours(domain.getOpeningHours())
                .averageRating(domain.getAverageRating() != null ? domain.getAverageRating() : 0.0)
                .reviewCount(domain.getReviewCount() != null ? domain.getReviewCount() : 0)
                .osmId(domain.getOsmId())
                .rating(domain.getRating())
                .reviewsCount(domain.getReviewsCount())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
