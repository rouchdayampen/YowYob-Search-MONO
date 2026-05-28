package com.yowyob.listing.domain.port.in;

import com.yowyob.listing.domain.model.Listing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'entrée (Use Case) — définit ce que le domaine Listing peut faire.
 * Implémenté par ListingApplicationService.
 * Appelé par les adaptateurs REST et Event.
 */
public interface ListingUseCase {

    Listing createListing(Listing listing);

    List<Listing> getAllListings();

    List<Listing> searchListings(LocalDateTime updatedAfter);

    Optional<Listing> getListingById(UUID id);

    List<Listing> getListingsBySellerId(UUID sellerId);

    Listing updateListing(UUID id, Listing listingDetails);

    void deleteListing(UUID id);
}
