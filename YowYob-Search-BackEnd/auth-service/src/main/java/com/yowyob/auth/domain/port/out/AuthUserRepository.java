package com.yowyob.auth.domain.port.out;

import com.yowyob.auth.domain.model.AuthUser;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie — interface vers la persistance des utilisateurs.
 * Implémenté par {@link com.yowyob.auth.adapter.out.persistence.UserJpaAdapter}.
 * Le domaine ne connaît pas JPA.
 */
public interface AuthUserRepository {

    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);

    AuthUser save(AuthUser user);

    Optional<AuthUser> findById(UUID id);
}
