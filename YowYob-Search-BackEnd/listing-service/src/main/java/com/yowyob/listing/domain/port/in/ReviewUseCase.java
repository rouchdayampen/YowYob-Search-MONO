package com.yowyob.listing.domain.port.in;

import com.yowyob.listing.domain.model.Review;
import com.yowyob.listing.dto.ReviewRequest;
import com.yowyob.listing.dto.ReviewResponse;
import com.yowyob.listing.dto.ReviewSummary;

import java.util.List;
import java.util.UUID;

/**
 * Port d'entrée (Use Case) — définit ce que le domaine Review peut faire.
 * Implémenté par ReviewApplicationService.
 * Note: ReviewRequest/Response/Summary sont des DTOs de l'application (couche adapter/in/rest),
 * conservés ici pour éviter une refactorisation excessive. Dans une refactorisation
 * complète, on utiliserait des objets de commande/query dédiés.
 */
public interface ReviewUseCase {

    ReviewResponse createReview(UUID listingId, ReviewRequest request);

    List<ReviewResponse> getReviews(UUID listingId);

    ReviewSummary getSummary(UUID listingId);

    void deleteReview(UUID listingId, UUID reviewId, String userId);
}
