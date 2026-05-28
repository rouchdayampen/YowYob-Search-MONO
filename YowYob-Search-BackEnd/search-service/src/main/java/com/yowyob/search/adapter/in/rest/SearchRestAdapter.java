package com.yowyob.search.adapter.in.rest;

import com.yowyob.search.domain.model.Product;
import com.yowyob.search.domain.port.in.SearchUseCase;
import com.yowyob.search.domain.port.out.GeoLocationPort;
import com.yowyob.search.dto.SearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Adaptateur REST pour les recherches de produits.
 * Remplace l'ancien {@code SearchController}.
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Endpoints for product search and indexing")
public class SearchRestAdapter {

    private final SearchUseCase searchUseCase;
    private final GeoLocationPort geoLocationPort; // Seulement si vraiment nécessaire ici, sinon dans le UseCase

    @GetMapping
    @Operation(summary = "Search products", description = "Search products in Elasticsearch with query, type and city filters")
    public Mono<SearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
            @RequestHeader(value = "Remote-Addr", required = false) String remoteAddr) {

        String ip = xForwardedFor != null ? xForwardedFor.split(",")[0].trim() : remoteAddr;
        if (ip == null) ip = "127.0.0.1";

        return searchUseCase.search(q, type, city, userId, ip);
    }

    @GetMapping("/autocomplete")
    @Operation(summary = "Autocomplete suggestions", description = "Get suggestions based on partial query")
    public Mono<List<String>> autocomplete(@RequestParam String q) {
        return searchUseCase.autocomplete(q);
    }

    @GetMapping("/proximity")
    @Operation(summary = "Search products by proximity", description = "Search products within a radius of a specified city")
    public Mono<SearchResponse> searchByProximity(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "10") Double radius) {
        return searchUseCase.searchByProximity(q, city, radius);
    }

    @GetMapping("/near-me")
    @Operation(summary = "Search products near user")
    public Mono<SearchResponse> searchNearMe(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String type) {

        if (ip != null && !ip.isEmpty() && (latitude == null || longitude == null)) {
            return geoLocationPort.getLocationFromIp(ip)
                    .flatMap(loc -> searchUseCase.searchByUserProximity(q, loc.getLatitude(), loc.getLongitude(), type))
                    .switchIfEmpty(searchUseCase.searchByUserProximity(q, latitude, longitude, type));
        }
        return searchUseCase.searchByUserProximity(q, latitude, longitude, type);
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get product details")
    public Mono<Product> getProductDetails(@PathVariable String id) {
        return searchUseCase.getProductById(id);
    }

    @PostMapping("/index")
    @Operation(summary = "Index product")
    public Mono<Product> indexProduct(@RequestBody Product product) {
        return searchUseCase.indexProduct(product);
    }

    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("Search Service Hexagonal is running!");
    }
}
