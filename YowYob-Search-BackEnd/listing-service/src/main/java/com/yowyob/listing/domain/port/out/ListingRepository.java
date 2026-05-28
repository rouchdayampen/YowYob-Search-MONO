package com.yowyob.listing.domain.port.out;

import com.yowyob.listing.domain.model.Listing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie — interface vers la persistance des Listings.
 * Implémenté par ListingJpaAdapter dans la couche adapter/out/persistence.
 * Le domaine ne connaît pas JPA.
 */
public interface ListingRepository {

    Listing save(Listing listing);

    Optional<Listing> findById(UUID id);

    List<Listing> findAll();

    List<Listing> findBySellerId(UUID sellerId);

    List<Listing> findByUpdatedAtAfter(LocalDateTime updatedAt);

    void delete(Listing listing);

    boolean existsById(UUID id);
}
