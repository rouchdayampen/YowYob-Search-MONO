package com.yowyob.user.domain.service;

import com.yowyob.user.domain.model.SearchHistory;
import com.yowyob.user.domain.model.UserProfile;
import com.yowyob.user.domain.port.in.UserUseCase;
import com.yowyob.user.domain.port.out.SearchHistoryRepository;
import com.yowyob.user.domain.port.out.UserProfileRepository;
import com.yowyob.user.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service applicatif du domaine User.
 * Implémente {@link UserUseCase}.
 * Dépend uniquement des ports de sortie (interfaces) — pas de JPA.
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService implements UserUseCase {

    private final UserProfileRepository userProfileRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    @Override
    public UserProfile getOrCreateProfile(UUID userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = UserProfile.builder()
                            .userId(userId)
                            .build();
                    return userProfileRepository.save(newProfile);
                });
    }

    @Override
    public UserProfile updateProfile(UUID userId, UserProfileDto dto) {
        UserProfile profile = getOrCreateProfile(userId);

        if (dto.getFirstName() != null)    profile.setFirstName(dto.getFirstName());
        if (dto.getEmail() != null)        profile.setEmail(dto.getEmail());
        if (dto.getLastName() != null)     profile.setLastName(dto.getLastName());
        if (dto.getBio() != null)          profile.setBio(dto.getBio());
        if (dto.getPhoneNumber() != null)  profile.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getAddress() != null)      profile.setAddress(dto.getAddress());
        if (dto.getCity() != null)         profile.setCity(dto.getCity());
        if (dto.getCountry() != null)      profile.setCountry(dto.getCountry());
        if (dto.getAvatarUrl() != null)    profile.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getSocialLinksJson() != null) profile.setSocialLinksJson(dto.getSocialLinksJson());

        return userProfileRepository.save(profile);
    }

    @Override
    public List<UserProfile> findAllProfiles() {
        return userProfileRepository.findAll();
    }

    @Override
    public List<UserProfile> searchProfiles(LocalDateTime updatedAfter) {
        if (updatedAfter == null) {
            return userProfileRepository.findAll();
        }
        return userProfileRepository.findByUpdatedAtAfter(updatedAfter);
    }

    @Override
    public void addSearchHistory(UUID userId, String query) {
        if (query == null || query.isBlank()) return;
        SearchHistory history = SearchHistory.builder()
                .userId(userId)
                .query(query.trim())
                .build();
        searchHistoryRepository.save(history);
    }

    @Override
    public List<SearchHistory> getSearchHistory(UUID userId) {
        return searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId);
    }

    @Override
    @Transactional
    public void clearSearchHistory(UUID userId) {
        searchHistoryRepository.deleteByUserId(userId);
    }
}
