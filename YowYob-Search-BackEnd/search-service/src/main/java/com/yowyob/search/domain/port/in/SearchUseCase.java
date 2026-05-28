package com.yowyob.search.domain.port.in;

import com.yowyob.search.domain.model.Product;
import com.yowyob.search.domain.model.SearchHistory;
import com.yowyob.search.dto.SearchResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Port d'entrée (Use Case) — opérations de recherche.
 * Implémenté par {@link com.yowyob.search.domain.service.SearchApplicationService}.
 */
public interface SearchUseCase {

    Mono<SearchResponse> search(String q, String type, String city, String userId, String ipAddress);

    Mono<List<String>> autocomplete(String q);

    Mono<SearchResponse> searchByProximity(String q, String city, Double radiusKm);

    Mono<SearchResponse> searchByUserProximity(String q, Double userLat, Double userLon, String type);

    Mono<Product> indexProduct(Product product);

    Mono<Product> getProductById(String id);

    Flux<SearchHistory> getSearchHistory(String userId);
}
