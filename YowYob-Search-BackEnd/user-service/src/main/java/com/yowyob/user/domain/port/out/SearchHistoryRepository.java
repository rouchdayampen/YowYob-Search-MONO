package com.yowyob.user.domain.port.out;

import com.yowyob.user.domain.model.SearchHistory;

import java.util.List;
import java.util.UUID;

/**
 * Port de sortie — interface vers la persistance des SearchHistory.
 * Implémenté par {@link com.yowyob.user.adapter.out.persistence.SearchHistoryJpaAdapter}.
 * Le domaine ne connaît pas JPA.
 */
public interface SearchHistoryRepository {

    SearchHistory save(SearchHistory history);

    List<SearchHistory> findByUserIdOrderBySearchedAtDesc(UUID userId);

    void deleteByUserId(UUID userId);
}
