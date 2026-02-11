package com.yowyob.crawler.Connector;

import com.yowyob.crawler.config.JwtTokenProvider;
import com.yowyob.crawler.model.ConnectorResponse;
import com.yowyob.crawler.model.ListingResponse;
import com.yowyob.crawler.model.ServiceDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "service.listing.enabled", havingValue = "true")
public class ListingServiceConnector implements ServiceConnector {

    private static final Logger log = LoggerFactory.getLogger(ListingServiceConnector.class);

    @Value("${service.listing.api.url}")
    private String baseUrl;

    @Value("${service.listing.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public ListingServiceConnector(RestTemplate restTemplate, JwtTokenProvider jwtTokenProvider) {
        this.restTemplate = restTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private String getSearchEndpoint() {
        // Current default: http://localhost:8083/api/listings
        // Target: http://localhost:8083/api/listing/search/documents
        if (baseUrl.endsWith("/api/listings")) {
            return baseUrl.replace("/api/listings", "/api/listing/search/documents");
        }
        return baseUrl + "/search/documents";
    }

    @Override
    public List<ConnectorResponse> fetchAll() {
        if (!enabled) {
            log.info("Listing Service connector is disabled");
            return Collections.emptyList();
        }

        try {
            String url = getSearchEndpoint();
            log.info("Fetching all listings from: {}", url);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ListingResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ListingResponse[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched {} listings", response.getBody().length);

                return Arrays.stream(response.getBody())
                        .map(listing -> ConnectorResponse.builder()
                                .data(listing)
                                .updatedAt(
                                        listing.getUpdatedAt() != null ? listing.getUpdatedAt() : LocalDateTime.now())
                                .sourceService("listing-service")
                                .build())
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();

        } catch (RestClientException e) {
            log.error("Error fetching listings: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ConnectorResponse> fetchIncremental(LocalDateTime lastSync) {
        if (!enabled) {
            return Collections.emptyList();
        }

        try {
            log.info("Fetching listings updated since: {}", lastSync);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = getSearchEndpoint() + "?updatedAfter=" +
                    lastSync.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            ResponseEntity<ListingResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ListingResponse[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched {} updated listings", response.getBody().length);

                return Arrays.stream(response.getBody())
                        .map(listing -> ConnectorResponse.builder()
                                .data(listing)
                                .updatedAt(
                                        listing.getUpdatedAt() != null ? listing.getUpdatedAt() : LocalDateTime.now())
                                .sourceService("listing-service")
                                .build())
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();

        } catch (RestClientException e) {
            log.error("Error fetching incremental listings: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public ServiceDocument transform(ConnectorResponse response) {
        ListingResponse listing = (ListingResponse) response.getData();

        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("sellerId", listing.getSellerId());
        if (listing.getStatus() != null) {
            metadata.put("status", listing.getStatus());
        }
        if (listing.getLatitude() != null) {
            metadata.put("latitude", listing.getLatitude());
        }
        if (listing.getLongitude() != null) {
            metadata.put("longitude", listing.getLongitude());
        }

        return ServiceDocument.builder()
                .serviceType(getServiceType())
                .serviceId("listing_" + listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .price(listing.getPrice())
                .category(listing.getCategory())
                .location(listing.getAddress())
                .indexedAt(LocalDateTime.now())
                .updatedAt(listing.getUpdatedAt())
                .metadata(metadata)
                .build();
    }

    @Override
    public String getServiceType() {
        return "listing";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Generate JWT Token
        String token = jwtTokenProvider.generateServiceToken();
        headers.set("Authorization", "Bearer " + token);

        return headers;
    }
}
