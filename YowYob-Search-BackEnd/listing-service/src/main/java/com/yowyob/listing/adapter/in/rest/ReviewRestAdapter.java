package com.yowyob.listing.adapter.in.rest;

import com.yowyob.listing.domain.port.in.ReviewUseCase;
import com.yowyob.listing.dto.ReviewRequest;
import com.yowyob.listing.dto.ReviewResponse;
import com.yowyob.listing.dto.ReviewSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Adaptateur d'entrée REST — expose les endpoints HTTP pour les Reviews.
 * Dépend uniquement du port d'entrée {@link ReviewUseCase} du domaine.
 */
@RestController
@RequestMapping("/api/listings/{listingId}/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewRestAdapter {

    private final ReviewUseCase reviewUseCase;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable UUID listingId,
            @RequestBody @Valid ReviewRequest request) {
        ReviewResponse response = reviewUseCase.createReview(listingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable UUID listingId) {
        return ResponseEntity.ok(reviewUseCase.getReviews(listingId));
    }

    @GetMapping("/summary")
    public ResponseEntity<ReviewSummary> getSummary(@PathVariable UUID listingId) {
        return ResponseEntity.ok(reviewUseCase.getSummary(listingId));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID listingId,
            @PathVariable UUID reviewId,
            @RequestParam String userId) {
        reviewUseCase.deleteReview(listingId, reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}
