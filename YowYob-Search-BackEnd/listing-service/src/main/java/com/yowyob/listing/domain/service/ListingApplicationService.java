package com.yowyob.listing.domain.service;

import com.yowyob.listing.domain.model.Listing;
import com.yowyob.listing.domain.model.ListingStatus;
import com.yowyob.listing.domain.port.in.ListingUseCase;
import com.yowyob.listing.domain.port.out.ListingEventPublisher;
import com.yowyob.listing.domain.port.out.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service applicatif du domaine Listing.
 * Implémente le port d'entrée {@link ListingUseCase}.
 * Dépend uniquement des ports de sortie (interfaces) — pas de JPA, pas de RabbitMQ.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ListingApplicationService implements ListingUseCase {

    private final ListingRepository listingRepository;
    private final ListingEventPublisher listingEventPublisher;

    @Override
    public Listing createListing(Listing listing) {
        listing.setCreatedAt(LocalDateTime.now());
        listing.setUpdatedAt(LocalDateTime.now());

        if (listing.getStatus() == null) {
            listing.setStatus(ListingStatus.ACTIVE);
        }
        if (listing.getSellerId() == null) {
            // ID système pour les listings venant du crawler
            listing.setSellerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        }
        if (listing.getAverageRating() == null) {
            listing.setAverageRating(0.0);
        }
        if (listing.getReviewCount() == null) {
            listing.setReviewCount(0);
        }

        Listing savedListing = listingRepository.save(listing);
        log.info("Listing created: id={}, title={}", savedListing.getId(), savedListing.getTitle());

        listingEventPublisher.publishCreated(savedListing);

        return savedListing;
    }

    @Override
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    @Override
    public List<Listing> searchListings(LocalDateTime updatedAfter) {
        if (updatedAfter == null) {
            return listingRepository.findAll();
        }
        return listingRepository.findByUpdatedAtAfter(updatedAfter);
    }

    @Override
    public Optional<Listing> getListingById(UUID id) {
        return listingRepository.findById(id);
    }

    @Override
    public List<Listing> getListingsBySellerId(UUID sellerId) {
        return listingRepository.findBySellerId(sellerId);
    }

    @Override
    public Listing updateListing(UUID id, Listing listingDetails) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found with id " + id));

        listing.setTitle(listingDetails.getTitle());
        listing.setDescription(listingDetails.getDescription());
        listing.setPrice(listingDetails.getPrice());
        listing.setCategory(listingDetails.getCategory());
        listing.setAddress(listingDetails.getAddress());
        listing.setLatitude(listingDetails.getLatitude());
        listing.setLongitude(listingDetails.getLongitude());
        listing.setImageUrl(listingDetails.getImageUrl());
        listing.setPhone(listingDetails.getPhone());
        listing.setOpeningHours(listingDetails.getOpeningHours());
        listing.setRating(listingDetails.getRating());
        listing.setReviewsCount(listingDetails.getReviewsCount());
        listing.setStatus(listingDetails.getStatus());
        listing.setUpdatedAt(LocalDateTime.now());

        Listing updatedListing = listingRepository.save(listing);
        log.info("Listing updated: id={}", id);

        listingEventPublisher.publishUpdated(updatedListing);

        return updatedListing;
    }

    @Override
    public void deleteListing(UUID id) {
        Listing listing = listingRepository.findById(id).orElse(null);
        if (listing != null) {
            listingRepository.delete(listing);
            listingEventPublisher.publishDeleted(listing);
            log.info("Listing deleted: id={}", id);
        }
    }
}
