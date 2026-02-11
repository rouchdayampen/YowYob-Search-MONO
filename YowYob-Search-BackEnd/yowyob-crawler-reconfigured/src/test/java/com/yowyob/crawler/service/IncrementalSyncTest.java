package com.yowyob.crawler.service;

import com.yowyob.crawler.Connector.ServiceConnector;
import com.yowyob.crawler.model.ConnectorResponse;
import com.yowyob.crawler.model.ServiceDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncrementalSyncTest {

    @Mock
    private ServiceConnector connector;
    @Mock
    private IndexerService indexerService;
    @Mock
    private SyncMetadataService metadataService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RestTemplate restTemplate;

    private CrawlerService crawlerService;

    @BeforeEach
    void setUp() {
        crawlerService = new CrawlerService(
                List.of(connector),
                indexerService,
                metadataService,
                objectMapper,
                restTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(crawlerService, "schedulerEnabled", true);
    }

    @Test
    void testIncrementalSyncFlow() {
        // Arrange
        String serviceType = "test-service";
        LocalDateTime lastSync = LocalDateTime.now().minusHours(1);
        LocalDateTime newSyncTime = LocalDateTime.now();

        // 1. Setup Connector
        when(connector.getServiceType()).thenReturn(serviceType);
        when(connector.isEnabled()).thenReturn(true);

        // 2. Setup Metadata Service to return a last sync date (triggering incremental)
        when(metadataService.getLastSyncDate(serviceType)).thenReturn(lastSync);

        // 3. Setup Connector to return data when called with fetchIncremental
        ConnectorResponse mockResponse = ConnectorResponse.builder()
                .data("some-data")
                .updatedAt(newSyncTime)
                .build();
        when(connector.fetchIncremental(lastSync)).thenReturn(List.of(mockResponse));

        // 4. Setup Transform
        ServiceDocument mockDoc = ServiceDocument.builder().id("doc-1").build();
        when(connector.transform(mockResponse)).thenReturn(mockDoc);

        // 5. Setup Indexer
        when(indexerService.bulkIndex(any())).thenReturn(1L);

        // Act
        crawlerService.crawlAllServices(false); // forceResync = false

        // Assert
        // Verify fetchIncremental was called with the correct date
        verify(connector).fetchIncremental(lastSync);

        // Verify Full Fetch was NOT called
        verify(connector, never()).fetchAll();

        // Verify Indexer was called
        verify(indexerService).bulkIndex(any());

        // Verify Metadata was updated
        verify(metadataService).updateLastSyncDate(eq(serviceType), any(LocalDateTime.class), eq(1L));
    }
}
