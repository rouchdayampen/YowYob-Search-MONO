package com.yowyob.user.domain.port.out;

import com.yowyob.user.domain.model.UserProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie — interface vers la persistance des UserProfiles.
 * Implémenté par {@link com.yowyob.user.adapter.out.persistence.UserProfileJpaAdapter}.
 * Le domaine ne connaît pas JPA.
 */
public interface UserProfileRepository {

    UserProfile save(UserProfile profile);

    Optional<UserProfile> findByUserId(UUID userId);

    List<UserProfile> findAll();

    List<UserProfile> findByUpdatedAtAfter(LocalDateTime updatedAt);
}
