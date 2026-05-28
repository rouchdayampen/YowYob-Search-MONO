package com.yowyob.listing.adapter.in.rest;

import com.yowyob.listing.domain.model.Listing;
import com.yowyob.listing.domain.model.ListingStatus;
import com.yowyob.listing.domain.port.in.ListingUseCase;
import com.yowyob.listing.dto.CrawlerListingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Adaptateur d'entrée REST — expose les endpoints HTTP pour les Listings.
 * Dépend uniquement du port d'entrée {@link ListingUseCase} du domaine.
 * Ne connaît pas JPA, RabbitMQ, ou les entités d'infrastructure.
 */
@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Listings", description = "Endpoints for managing listings (products/services)")
public class ListingRestAdapter {

    private final ListingUseCase listingUseCase;

    @PostMapping
    @Operation(summary = "Create a listing", description = "Creates a new listing for a product or service")
    public ResponseEntity<Listing> createListing(@RequestBody Listing listing) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingUseCase.createListing(listing));
    }

    @GetMapping
    @Operation(summary = "Get all listings", description = "Retrieve a list of all available listings")
    public ResponseEntity<List<Listing>> getAllListings() {
        return ResponseEntity.ok(listingUseCase.getAllListings());
    }

    @GetMapping("/search/documents")
    @Operation(summary = "Search Documents", description = "Internal endpoint for Crawler Service to fetch documents")
    public ResponseEntity<List<Listing>> searchDocuments(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAfter) {
        return ResponseEntity.ok(listingUseCase.searchListings(updatedAfter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get listing by ID", description = "Retrieve a specific listing by its unique identifier")
    public ResponseEntity<Listing> getListingById(@PathVariable UUID id) {
        return listingUseCase.getListingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get listings by seller", description = "Retrieve all listings belonging to a specific seller")
    public ResponseEntity<List<Listing>> getListingsBySeller(@PathVariable UUID sellerId) {
        return ResponseEntity.ok(listingUseCase.getListingsBySellerId(sellerId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update listing", description = "Update details of an existing listing")
    public ResponseEntity<Listing> updateListing(@PathVariable UUID id, @RequestBody Listing listing) {
        return ResponseEntity.ok(listingUseCase.updateListing(id, listing));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete listing", description = "Remove a listing from the platform")
    public ResponseEntity<Void> deleteListing(@PathVariable UUID id) {
        listingUseCase.deleteListing(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public String health() {
        return "Listing Service is running! [Hexagonal Architecture]";
    }

    @Deprecated(since = "2.0", forRemoval = true)
    @PostMapping("/crawler/ingest")
    @Operation(summary = "Ingest crawler listing", description = "DEPRECATED Internal endpoint - use Kafka event instead")
    public ResponseEntity<Listing> ingestCrawlerListing(@RequestBody CrawlerListingRequest request) {
        log.warn("DEPRECATED endpoint /crawler/ingest called — migrate to Kafka topic crawler.listings.events");
        Listing listing = Listing.builder()
                .title(request.getTitle())
                .description(request.getDescription() != null ? request.getDescription() : "Source: " + request.getSource())
                .price(request.getPrice() != null ? request.getPrice() : 0.0)
                .category(request.getCategory() != null ? request.getCategory() : "GENERAL")
                .address(request.getCity() != null ? request.getCity() + ", " + request.getCountry() : request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(ListingStatus.ACTIVE)
                .sellerId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(listingUseCase.createListing(listing));
    }
}
