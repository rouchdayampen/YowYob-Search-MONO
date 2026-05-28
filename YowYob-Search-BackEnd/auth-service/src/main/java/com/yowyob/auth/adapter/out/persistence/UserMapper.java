package com.yowyob.auth.adapter.out.persistence;

import com.yowyob.auth.domain.model.AuthUser;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle de domaine {@link AuthUser} et l'entité JPA {@link UserJpaEntity}.
 */
@Component
public class UserMapper {

    public AuthUser toDomain(UserJpaEntity entity) {
        if (entity == null) return null;
        return AuthUser.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .phone(entity.getPhone())
                .avatarUrl(entity.getAvatarUrl())
                .role(AuthUser.Role.valueOf(entity.getRole().name()))
                .emailVerified(entity.getEmailVerified())
                .status(AuthUser.Status.valueOf(entity.getStatus().name()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserJpaEntity toEntity(AuthUser domain) {
        if (domain == null) return null;
        return UserJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .phone(domain.getPhone())
                .avatarUrl(domain.getAvatarUrl())
                .role(domain.getRole() != null
                        ? UserJpaEntity.Role.valueOf(domain.getRole().name())
                        : UserJpaEntity.Role.USER)
                .emailVerified(domain.getEmailVerified() != null ? domain.getEmailVerified() : false)
                .status(domain.getStatus() != null
                        ? UserJpaEntity.Status.valueOf(domain.getStatus().name())
                        : UserJpaEntity.Status.ACTIVE)
                .build();
    }
}
