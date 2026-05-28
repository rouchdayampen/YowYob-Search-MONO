package com.yowyob.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface Spring Data JPA pour les UserProfiles.
 * Connue uniquement de l'adaptateur de persistance.
 */
@Repository
public interface UserProfileJpaRepository extends JpaRepository<UserProfileJpaEntity, UUID> {

    Optional<UserProfileJpaEntity> findByUserId(UUID userId);

    List<UserProfileJpaEntity> findByUpdatedAtAfter(LocalDateTime updatedAt);
}
