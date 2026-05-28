package com.yowyob.listing.adapter.out.persistence;

import com.yowyob.listing.domain.model.Review;
import com.yowyob.listing.domain.port.out.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur de sortie — implémente le port {@link ReviewRepository} du domaine
 * en déléguant à Spring Data JPA via {@link ReviewJpaRepository}.
 */
@Component
@RequiredArgsConstructor
public class ReviewJpaAdapter implements ReviewRepository {

    private final ReviewJpaRepository jpaRepository;
    private final ListingJpaRepository listingJpaRepository;
    private final ReviewMapper mapper;

    @Override
    public Review save(Review review) {
        // Récupère l'entité JPA Listing pour la relation @ManyToOne
        ListingJpaEntity listingEntity = listingJpaRepository.findById(review.getListingId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Listing introuvable : " + review.getListingId()));

        ReviewJpaEntity entity = mapper.toEntity(review, listingEntity);
        ReviewJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Review> findByListingIdOrderByCreatedAtDesc(UUID listingId) {
        return jpaRepository.findByListingIdOrderByCreatedAtDesc(listingId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Double> calculateAverageRating(UUID listingId) {
        return jpaRepository.calculateAverageRating(listingId);
    }

    @Override
    public long countByListingId(UUID listingId) {
        return jpaRepository.countByListingId(listingId);
    }

    @Override
    public boolean existsByListingIdAndUserId(UUID listingId, String userId) {
        return jpaRepository.existsByListingIdAndUserId(listingId, userId);
    }

    @Override
    public void delete(Review review) {
        jpaRepository.deleteById(review.getId());
    }
}
