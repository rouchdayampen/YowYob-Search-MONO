package com.yowyob.listing.domain.port.out;

import com.yowyob.listing.domain.model.Review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie — interface vers la persistance des Reviews.
 * Implémenté par ReviewJpaAdapter dans la couche adapter/out/persistence.
 * Le domaine ne connaît pas JPA.
 */
public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(UUID id);

    List<Review> findByListingIdOrderByCreatedAtDesc(UUID listingId);

    Optional<Double> calculateAverageRating(UUID listingId);

    long countByListingId(UUID listingId);

    boolean existsByListingIdAndUserId(UUID listingId, String userId);

    void delete(Review review);
}
