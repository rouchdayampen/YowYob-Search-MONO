package com.yowyob.listing.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Interface Spring Data JPA pour les Listings.
 * Connue uniquement de l'adaptateur de persistance.
 */
@Repository
public interface ListingJpaRepository extends JpaRepository<ListingJpaEntity, UUID> {

    List<ListingJpaEntity> findBySellerId(UUID sellerId);

    List<ListingJpaEntity> findByUpdatedAtAfter(LocalDateTime updatedAt);
}
