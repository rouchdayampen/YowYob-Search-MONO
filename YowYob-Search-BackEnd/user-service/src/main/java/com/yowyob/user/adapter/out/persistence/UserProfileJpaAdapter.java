package com.yowyob.user.adapter.out.persistence;

import com.yowyob.user.domain.model.UserProfile;
import com.yowyob.user.domain.port.out.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur de persistance pour {@link UserProfile}.
 * Implémente le port de sortie {@link UserProfileRepository} du domaine.
 * Traduit entre les modèles de domaine et les entités JPA via {@link UserProfileMapper}.
 */
@Component
@RequiredArgsConstructor
public class UserProfileJpaAdapter implements UserProfileRepository {

    private final UserProfileJpaRepository jpaRepository;
    private final UserProfileMapper mapper;

    @Override
    public Optional<UserProfile> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public UserProfile save(UserProfile profile) {
        UserProfileJpaEntity entity = mapper.toEntity(profile);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<UserProfile> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserProfile> findByUpdatedAtAfter(LocalDateTime updatedAfter) {
        return jpaRepository.findByUpdatedAtAfter(updatedAfter).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
