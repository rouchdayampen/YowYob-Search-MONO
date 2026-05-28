package com.yowyob.user.adapter.out.persistence;

import com.yowyob.user.domain.model.UserProfile;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle de domaine {@link UserProfile} et l'entité JPA {@link UserProfileJpaEntity}.
 * Isole complètement le domaine de JPA.
 */
@Component
public class UserProfileMapper {

    public UserProfile toDomain(UserProfileJpaEntity entity) {
        if (entity == null) return null;
        return UserProfile.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .bio(entity.getBio())
                .phoneNumber(entity.getPhoneNumber())
                .address(entity.getAddress())
                .city(entity.getCity())
                .country(entity.getCountry())
                .avatarUrl(entity.getAvatarUrl())
                .socialLinksJson(entity.getSocialLinksJson())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserProfileJpaEntity toEntity(UserProfile domain) {
        if (domain == null) return null;
        return UserProfileJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .email(domain.getEmail())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .bio(domain.getBio())
                .phoneNumber(domain.getPhoneNumber())
                .address(domain.getAddress())
                .city(domain.getCity())
                .country(domain.getCountry())
                .avatarUrl(domain.getAvatarUrl())
                .socialLinksJson(domain.getSocialLinksJson())
                .build();
    }
}
