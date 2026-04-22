package com.yowyob.crawler.Connector;

import com.yowyob.crawler.config.JwtTokenProvider;
import com.yowyob.crawler.model.ConnectorResponse;
import com.yowyob.crawler.model.ServiceDocument;
import com.yowyob.crawler.model.UserResponse;
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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "service.user.enabled", havingValue = "true")
public class UserServiceConnector implements ServiceConnector {

    private static final Logger log = LoggerFactory.getLogger(UserServiceConnector.class);

    @Value("${service.user.api.url}")
    private String baseUrl;

    @Value("${service.user.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserServiceConnector(RestTemplate restTemplate, JwtTokenProvider jwtTokenProvider) {
        this.restTemplate = restTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private String getSearchEndpoint() {
        // Real endpoint: /api/users/search/documents (note: with 's')
        if (baseUrl.endsWith("/api/users")) {
            return baseUrl + "/search/documents";
        }
        return baseUrl + "/search/documents";
    }

    @Override
    public List<ConnectorResponse> fetchAll() {
        if (!enabled) {
            log.info("User Service connector is disabled");
            return Collections.emptyList();
        }

        try {
            String url = getSearchEndpoint();
            log.info("Fetching all users from: {}", url);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Expecting ServiceDocument[] or UserResponse[] depending on what the endpoint
            // returns.
            // Requirement says: GET /api/{service}/search/documents
            // Assuming it returns a list of "documents" or "users".
            // Since we still need to transform, let's assume it returns standard
            // UserResponse objects for now
            // OR if the endpoint is "search/documents", maybe it returns ServiceDocument
            // directly?
            // The prompt says: "Faire des requêtes HTTP vers GET
            // /api/{service}/search/documents"
            // Usually search endpoints return the target entity. Let's assume UserResponse
            // for compatibility with existing Transform logic.

            ResponseEntity<UserResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserResponse[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched {} users", response.getBody().length);

                return Arrays.stream(response.getBody())
                        .map(user -> ConnectorResponse.builder()
                                .data(user)
                                .updatedAt(user.getUpdatedAt())
                                .sourceService("user-service")
                                .build())
                        .collect(Collectors.toList());
            }

            log.warn("Empty response from User Service");
            return Collections.emptyList();

        } catch (RestClientException e) {
            log.error("Error fetching users from User Service: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ConnectorResponse> fetchIncremental(LocalDateTime lastSync) {
        if (!enabled) {
            return Collections.emptyList();
        }

        try {
            log.info("Fetching users updated since: {}", lastSync);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = getSearchEndpoint() + "?updatedAfter=" +
                    lastSync.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            ResponseEntity<UserResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserResponse[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched {} updated users", response.getBody().length);

                return Arrays.stream(response.getBody())
                        .filter(user -> user.getUpdatedAt().isAfter(lastSync)) // Double check
                        .map(user -> ConnectorResponse.builder()
                                .data(user)
                                .updatedAt(user.getUpdatedAt())
                                .sourceService("user-service")
                                .build())
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();

        } catch (RestClientException e) {
            log.error("Error fetching incremental users: {}", e.getMessage());
            return fetchAll().stream()
                    .filter(response -> response.getUpdatedAt().isAfter(lastSync))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public ServiceDocument transform(ConnectorResponse response) {
        UserResponse user = (UserResponse) response.getData();

        return ServiceDocument.builder()
                .serviceType(getServiceType())
                .serviceId("user_" + user.getId())
                .title(user.getFirstName() + " " + user.getLastName())
                .description("User: " + user.getEmail())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhoneNumber())
                .role(user.getRole())
                .indexedAt(LocalDateTime.now())
                .updatedAt(user.getUpdatedAt())
                .metadata(java.util.Map.of(
                        "userId", user.getUserId() != null ? user.getUserId().toString() : "",
                        "bio", user.getBio() != null ? user.getBio() : "",
                        "city", user.getCity() != null ? user.getCity() : "",
                        "country", user.getCountry() != null ? user.getCountry() : "",
                        "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : ""))
                .build();
    }

    @Override
    public String getServiceType() {
        return "user";
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