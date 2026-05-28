package com.yowyob.user.domain.port.in;

import com.yowyob.user.domain.model.SearchHistory;
import com.yowyob.user.domain.model.UserProfile;
import com.yowyob.user.dto.UserProfileDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Port d'entrée (Use Case) — définit toutes les opérations du domaine User.
 * Implémenté par {@link com.yowyob.user.domain.service.UserApplicationService}.
 * Appelé par les adaptateurs REST.
 */
public interface UserUseCase {

    UserProfile getOrCreateProfile(UUID userId);

    UserProfile updateProfile(UUID userId, UserProfileDto dto);

    List<UserProfile> findAllProfiles();

    List<UserProfile> searchProfiles(LocalDateTime updatedAfter);

    void addSearchHistory(UUID userId, String query);

    List<SearchHistory> getSearchHistory(UUID userId);

    void clearSearchHistory(UUID userId);
}
