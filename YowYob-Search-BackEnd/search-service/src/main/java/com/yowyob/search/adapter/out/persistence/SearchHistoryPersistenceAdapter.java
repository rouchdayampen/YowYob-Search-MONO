package com.yowyob.search.adapter.out.persistence;

import com.yowyob.search.domain.model.SearchHistory;
import com.yowyob.search.domain.port.out.SearchHistoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Adaptateur de persistance de l'historique implémentant {@link SearchHistoryPort}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryPersistenceAdapter implements SearchHistoryPort {

    private final SearchHistoryRepository repository;

    @Override
    public Mono<Void> saveSearch(String userId, String query, String type, String city) {
        if (userId == null || userId.isEmpty()) {
            return Mono.empty();
        }
        SearchHistoryDocument history = SearchHistoryDocument.builder()
                .userId(userId)
                .query(query)
                .type(type)
                .city(city)
                .timestamp(Instant.now())
                .build();

        return repository.save(history)
                .doOnError(e -> log.error("Failed to save search history", e))
                .then();
    }

    @Override
    public Flux<SearchHistory> getUserHistory(String userId) {
        return repository.findByUserIdOrderByTimestampDesc(userId, PageRequest.of(0, 50))
                .map(doc -> SearchHistory.builder()
                        .id(doc.getId())
                        .userId(doc.getUserId())
                        .query(doc.getQuery())
                        .type(doc.getType())
                        .city(doc.getCity())
                        .timestamp(doc.getTimestamp())
                        .build());
    }
}
