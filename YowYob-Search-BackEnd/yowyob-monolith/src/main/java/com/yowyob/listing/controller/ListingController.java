package com.yowyob.listing.controller;

import com.yowyob.listing.entity.Listing;
import com.yowyob.listing.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
@Tag(name = "Listings", description = "Endpoints for managing listings (products/services)")
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    @Operation(summary = "Create a listing", description = "Creates a new listing for a product or service")
    public ResponseEntity<Listing> createListing(@RequestBody Listing listing) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.createListing(listing));
    }

    @GetMapping
    @Operation(summary = "Get all listings", description = "Retrieve a list of all available listings")
    public ResponseEntity<List<Listing>> getAllListings() {
        return ResponseEntity.ok(listingService.getAllListings());
    }

    @GetMapping("/search/documents")
    @Operation(summary = "Search Documents", description = "Internal endpoint for Crawler Service to fetch documents")
    public ResponseEntity<List<Listing>> searchDocuments(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime updatedAfter) {
        return ResponseEntity.ok(listingService.searchListings(updatedAfter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get listing by ID", description = "Retrieve a specific listing by its unique identifier")
    public ResponseEntity<Listing> getListingById(@PathVariable UUID id) {
        return listingService.getListingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get listings by seller", description = "Retrieve all listings belonging to a specific seller")
    public ResponseEntity<List<Listing>> getListingsBySeller(@PathVariable UUID sellerId) {
        return ResponseEntity.ok(listingService.getListingsBySellerId(sellerId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update listing", description = "Update details of an existing listing")
    public ResponseEntity<Listing> updateListing(@PathVariable UUID id, @RequestBody Listing listing) {
        return ResponseEntity.ok(listingService.updateListing(id, listing));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete listing", description = "Remove a listing from the platform")
    public ResponseEntity<Void> deleteListing(@PathVariable UUID id) {
        listingService.deleteListing(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public String health() {
        return "Listing Service is running!";
    }
}
