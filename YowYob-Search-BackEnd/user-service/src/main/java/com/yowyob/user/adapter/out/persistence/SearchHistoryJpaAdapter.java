package com.yowyob.user.adapter.out.persistence;

import com.yowyob.user.domain.model.SearchHistory;
import com.yowyob.user.domain.port.out.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur de persistance pour {@link SearchHistory}.
 * Implémente le port de sortie {@link SearchHistoryRepository} du domaine.
 */
@Component
@RequiredArgsConstructor
public class SearchHistoryJpaAdapter implements SearchHistoryRepository {

    private final SearchHistoryJpaRepository jpaRepository;
    private final SearchHistoryMapper mapper;

    @Override
    public SearchHistory save(SearchHistory history) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(history)));
    }

    @Override
    public List<SearchHistory> findByUserIdOrderBySearchedAtDesc(UUID userId) {
        return jpaRepository.findByUserIdOrderBySearchedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
