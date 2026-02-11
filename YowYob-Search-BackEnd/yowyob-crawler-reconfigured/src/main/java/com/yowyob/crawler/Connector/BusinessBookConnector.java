package com.yowyob.crawler.Connector;

import com.yowyob.crawler.model.ConnectorResponse;
import com.yowyob.crawler.model.BusinessBookResponse;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "service.businessbook.enabled", havingValue = "true")
public class BusinessBookConnector implements ServiceConnector {

    private static final Logger log = LoggerFactory.getLogger(BusinessBookConnector.class);

    @Value("${service.businessbook.api.url}")
    private String apiUrl;

    @Value("${service.businessbook.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;

    @Autowired
    public BusinessBookConnector(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<ConnectorResponse> fetchAll() {
        if (!enabled) {
            log.info("BusinessBook connector is disabled");
            return Collections.emptyList();
        }

        try {
            log.info("Fetching data from BusinessBook API: {}", apiUrl);

            ResponseEntity<BusinessBookResponse> response = restTemplate.getForEntity(apiUrl,
                    BusinessBookResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<BusinessBookResponse.BusinessBookDocument> docs = response.getBody().getDocuments();
                log.info("Successfully fetched {} documents from BusinessBook", docs.size());

                return docs.stream()
                        .map(doc -> ConnectorResponse.builder()
                                .data(doc)
                                .updatedAt(doc.getUpdatedAt() != null ? doc.getUpdatedAt() : LocalDateTime.now())
                                .sourceService("businessbook")
                                .build())
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();

        } catch (RestClientException e) {
            log.error("Error fetching from BusinessBook: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ConnectorResponse> fetchIncremental(LocalDateTime lastSync) {
        // BusinessBook API might not support incremental, so we fallback to fetchAll
        // or filter locally
        return fetchAll().stream()
                .filter(res -> res.getUpdatedAt().isAfter(lastSync))
                .collect(Collectors.toList());
    }

    @Override
    public ServiceDocument transform(ConnectorResponse response) {
        BusinessBookResponse.BusinessBookDocument doc = (BusinessBookResponse.BusinessBookDocument) response.getData();

        return ServiceDocument.builder()
                .serviceType(getServiceType())
                .serviceId(doc.getId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .indexedAt(LocalDateTime.now())
                .updatedAt(doc.getUpdatedAt())
                .metadata(doc.getContent())
                .build();
    }

    @Override
    public String getServiceType() {
        return "businessbook";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
