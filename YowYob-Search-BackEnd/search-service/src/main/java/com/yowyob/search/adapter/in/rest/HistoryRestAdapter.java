package com.yowyob.search.adapter.in.rest;

import com.yowyob.search.domain.model.SearchHistory;
import com.yowyob.search.domain.port.in.SearchUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Adaptateur REST pour la consultation de l'historique de recherche.
 * Remplace l'ancien {@code HistoryController}.
 */
@RestController
@RequestMapping("/api/search/history")
@RequiredArgsConstructor
@Slf4j
public class HistoryRestAdapter {

    private final SearchUseCase searchUseCase;

    @GetMapping
    public Flux<SearchHistory> getHistory(@RequestHeader(value = "X-User-Id") String userId) {
        log.info("Fetching history for User ID: {}", userId);
        return searchUseCase.getSearchHistory(userId)
                .doOnComplete(() -> log.info("History fetch completed for user: {}", userId))
                .doOnError(e -> log.error("Error fetching history for user: {}", userId, e));
    }
}
