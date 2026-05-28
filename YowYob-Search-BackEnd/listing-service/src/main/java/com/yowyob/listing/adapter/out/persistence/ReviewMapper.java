package com.yowyob.listing.adapter.out.persistence;

import com.yowyob.listing.domain.model.Review;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle de domaine {@link Review} et l'entité JPA {@link ReviewJpaEntity}.
 */
@Component
public class ReviewMapper {

    public Review toDomain(ReviewJpaEntity entity) {
        if (entity == null) return null;
        return Review.builder()
                .id(entity.getId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .userId(entity.getUserId())
                .listingId(entity.getListing() != null ? entity.getListing().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ReviewJpaEntity toEntity(Review domain, ListingJpaEntity listingEntity) {
        if (domain == null) return null;
        return ReviewJpaEntity.builder()
                .id(domain.getId())
                .rating(domain.getRating())
                .comment(domain.getComment())
                .userId(domain.getUserId())
                .createdAt(domain.getCreatedAt())
                .listing(listingEntity)
                .build();
    }
}
