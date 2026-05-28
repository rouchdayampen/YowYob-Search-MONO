package com.yowyob.auth.adapter.out.persistence;

import com.yowyob.auth.domain.model.AuthUser;
import com.yowyob.auth.domain.port.out.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur de persistance implémentant {@link AuthUserRepository}.
 * Traduit entre {@link AuthUser} (domaine) et {@link UserJpaEntity} (JPA).
 */
@Component
@RequiredArgsConstructor
public class UserJpaAdapter implements AuthUserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public AuthUser save(AuthUser user) {
        UserJpaEntity entity = mapper.toEntity(user);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<AuthUser> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
