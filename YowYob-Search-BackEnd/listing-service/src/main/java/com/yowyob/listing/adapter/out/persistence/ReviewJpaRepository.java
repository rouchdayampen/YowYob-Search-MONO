package com.yowyob.listing.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface Spring Data JPA pour les Reviews.
 * Connue uniquement de l'adaptateur de persistance.
 */
@Repository
public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, UUID> {

    List<ReviewJpaEntity> findByListingIdOrderByCreatedAtDesc(UUID listingId);

    @Query("SELECT AVG(r.rating) FROM ReviewJpaEntity r WHERE r.listing.id = :listingId")
    Optional<Double> calculateAverageRating(@Param("listingId") UUID listingId);

    long countByListingId(UUID listingId);

    boolean existsByListingIdAndUserId(UUID listingId, String userId);
}
