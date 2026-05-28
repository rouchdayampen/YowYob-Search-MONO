package com.yowyob.search.domain.port.out;

import com.yowyob.search.domain.model.SearchHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Port de sortie — interface vers la persistance de l'historique de recherche.
 * Implémenté par {@link com.yowyob.search.adapter.out.persistence.SearchHistoryPersistenceAdapter}.
 */
public interface SearchHistoryPort {

    Mono<Void> saveSearch(String userId, String query, String type, String city);

    Flux<SearchHistory> getUserHistory(String userId);
}
