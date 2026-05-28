package com.yowyob.listing.adapter.out.persistence;

import com.yowyob.listing.domain.model.Listing;
import com.yowyob.listing.domain.port.out.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur de sortie — implémente le port {@link ListingRepository} du domaine
 * en déléguant à Spring Data JPA via {@link ListingJpaRepository}.
 */
@Component
@RequiredArgsConstructor
public class ListingJpaAdapter implements ListingRepository {

    private final ListingJpaRepository jpaRepository;
    private final ListingMapper mapper;

    @Override
    public Listing save(Listing listing) {
        ListingJpaEntity entity = mapper.toEntity(listing);
        ListingJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Listing> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Listing> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Listing> findBySellerId(UUID sellerId) {
        return jpaRepository.findBySellerId(sellerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Listing> findByUpdatedAtAfter(LocalDateTime updatedAt) {
        return jpaRepository.findByUpdatedAtAfter(updatedAt).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Listing listing) {
        jpaRepository.deleteById(listing.getId());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
