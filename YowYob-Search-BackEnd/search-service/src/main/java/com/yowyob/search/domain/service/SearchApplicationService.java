package com.yowyob.search.domain.service;

import com.yowyob.search.domain.model.Product;
import com.yowyob.search.domain.port.in.SearchUseCase;
import com.yowyob.search.domain.port.out.GeoLocationPort;
import com.yowyob.search.domain.port.out.ProductIndexPort;
import com.yowyob.search.domain.port.out.SearchHistoryPort;
import com.yowyob.search.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service applicatif du domaine Search.
 * Implémente {@link SearchUseCase}.
 * Orchestre les ports de sortie et encapsule la logique métier (notamment {@link KeywordParser}).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchApplicationService implements SearchUseCase {

    private final ProductIndexPort productIndexPort;
    private final GeoLocationPort geoLocationPort;
    private final SearchHistoryPort searchHistoryPort;
    private final KeywordParser keywordParser;

    @Override
    public Mono<SearchResponse> search(String q, String type, String city, String userId, String ipAddress) {
        log.info("Processing search - query: '{}', type: '{}', city: '{}'", q, type, city);

        KeywordParser.ParsedQueryResult parsed = keywordParser.parseWithCity(q);

        String finalCity = city != null ? city : parsed.extractedCity;
        String finalType = type != null ? type : parsed.inferredCategory;
        String searchQuery = parsed.query;

        if (parsed.isProximitySearch && finalCity != null) {
            return searchByProximity(searchQuery, finalCity, parsed.proximityRadius)
                    .doOnSuccess(r -> saveHistoryAsync(userId, q, type, city));
        }

        return productIndexPort.searchByText(searchQuery)
                .filter(p -> finalType == null || finalType.equalsIgnoreCase(p.getType()) || finalType.equalsIgnoreCase(p.getCategory()))
                .filter(p -> finalCity == null || finalCity.equalsIgnoreCase(p.getCity()))
                .collectList()
                .map(products -> buildResponse(products, searchQuery, finalCity, finalType))
                .doOnSuccess(r -> saveHistoryAsync(userId, q, type, city));
    }

    @Override
    public Mono<List<String>> autocomplete(String q) {
        return productIndexPort.searchByText(q)
                .map(Product::getTitle)
                .distinct()
                .take(10)
                .collectList();
    }

    @Override
    public Mono<SearchResponse> searchByProximity(String q, String city, Double radiusKm) {
        if (city == null) return Mono.just(new SearchResponse());

        return geoLocationPort.geocode(city)
                .flatMap(loc -> searchByUserProximity(q, loc.getLatitude(), loc.getLongitude(), null))
                .switchIfEmpty(search(q, null, city, null, null));
    }

    @Override
    public Mono<SearchResponse> searchByUserProximity(String q, Double userLat, Double userLon, String type) {
        if (userLat == null || userLon == null) {
            return search(q, type, null, null, null);
        }

        KeywordParser.ParsedQueryResult parsed = keywordParser.parseWithCity(q);
        String finalType = type != null ? type : parsed.inferredCategory;
        Double radius = parsed.proximityRadius != null ? parsed.proximityRadius : 10.0;

        return productIndexPort.searchByText(parsed.query)
                .filter(p -> finalType == null || finalType.equalsIgnoreCase(p.getType()) || finalType.equalsIgnoreCase(p.getCategory()))
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                .filter(p -> calculateDistance(userLat, userLon, p.getLatitude(), p.getLongitude()) <= radius)
                .collectList()
                .map(products -> buildResponse(products, parsed.query, "Proximity Search", finalType));
    }

    @Override
    public Mono<Product> indexProduct(Product product) {
        if (product.getLatitude() == null || product.getLongitude() == null) {
            if (product.getCity() != null) {
                return geoLocationPort.geocode(product.getCity())
                        .flatMap(loc -> {
                            product.setLatitude(loc.getLatitude());
                            product.setLongitude(loc.getLongitude());
                            return productIndexPort.save(product);
                        })
                        .switchIfEmpty(productIndexPort.save(product));
            }
        }
        return productIndexPort.save(product);
    }

    @Override
    public Mono<Product> getProductById(String id) {
        return productIndexPort.findById(id);
    }

    @Override
    public Flux<com.yowyob.search.domain.model.SearchHistory> getSearchHistory(String userId) {
        return searchHistoryPort.getUserHistory(userId);
    }

    private SearchResponse buildResponse(List<Product> products, String query, String city, String type) {
        SearchResponse response = new SearchResponse();
        response.setSuccess(true);
        response.setResults(products.stream().map(p -> {
            SearchResponse.ProductDto result = new SearchResponse.ProductDto();
            result.setId(p.getId());
            result.setTitle(p.getTitle());
            result.setDescription(p.getDescription());
            result.setPrice(p.getPrice());
            result.setServiceType(p.getServiceType());
            result.setType(p.getType());
            result.setCategory(p.getCategory());
            result.setCity(p.getCity());
            result.setRating(p.getRating());
            result.setLatitude(p.getLatitude());
            result.setLongitude(p.getLongitude());
            result.setImages(p.getImages());
            return result;
        }).collect(Collectors.toList()));
        response.setTotal(products.size());
        response.setQuery(query);
        return response;
    }

    private void saveHistoryAsync(String userId, String q, String type, String city) {
        if (userId != null && !userId.isEmpty() && q != null && !q.isEmpty()) {
            searchHistoryPort.saveSearch(userId, q, type, city).subscribe();
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la terre en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
