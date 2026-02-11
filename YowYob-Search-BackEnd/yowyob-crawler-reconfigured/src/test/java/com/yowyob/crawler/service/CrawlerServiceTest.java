package com.yowyob.crawler.service;

import com.yowyob.crawler.Connector.ServiceConnector;
import com.yowyob.crawler.model.ConnectorResponse;
import com.yowyob.crawler.model.ServiceDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrawlerServiceTest {

        @Mock
        private ServiceConnector userConnector;

        @Mock
        private IndexerService indexerService;

        @Mock
        private SyncMetadataService metadataService;

        @Mock
        private ObjectMapper objectMapper;

        @Mock
        private RestTemplate restTemplate;

        // logic...

        private CrawlerService crawlerService;

        @BeforeEach
        void setUp() {
                // Setup mocks behavior
                when(userConnector.getServiceType()).thenReturn("user");

                crawlerService = new CrawlerService(
                                List.of(userConnector),
                                indexerService,
                                metadataService,
                                objectMapper,
                                restTemplate);
        }

        @Test
        void crawlAllServices_ShouldFetchAndIndex_WhenDataAvailable() {
                // Arrange
                ConnectorResponse mockResponse = ConnectorResponse.builder()
                                .data(new Object())
                                .updatedAt(LocalDateTime.now())
                                .sourceService("user-service")
                                .build();

                ServiceDocument mockDocument = ServiceDocument.builder()
                                .id("test-id")
                                .build();

                when(metadataService.getLastSyncDate("user")).thenReturn(null); // Full crawl
                when(userConnector.fetchAll()).thenReturn(List.of(mockResponse));
                when(userConnector.transform(any())).thenReturn(mockDocument);
                when(indexerService.bulkIndex(anyList())).thenReturn(1L);

                // Act
                // Force sync to bypass scheduler flag check if internal field default is false
                // But since we instantiate manually, the @Value field is 0/false/null.
                // We need to use reflection or constructor logic.
                // The constructor doesn't set the flag. Reflection is needed or a setter.
                // Actually, the method crawlAllServices checks 'schedulerEnabled' private
                // field.
                // Since we can't easily set private field without reflection in unit test (or
                // Spring context),
                // we can use the `crawlService` method directly which is public and what
                // crawlAllServices calls.

                crawlerService.crawlService(userConnector, false);

                // Assert
                verify(userConnector).fetchAll();
                verify(indexerService).bulkIndex(anyList());
                verify(metadataService).updateLastSyncDate(eq("user"), any(), eq(1L));
        }

        @Test
        void crawlAllServices_ShouldDoIncremental_WhenLastSyncExists() {
                // Arrange
                LocalDateTime lastSync = LocalDateTime.now().minusDays(1);
                when(metadataService.getLastSyncDate("user")).thenReturn(lastSync);

                ConnectorResponse mockResponse = ConnectorResponse.builder()
                                .data(new Object())
                                .updatedAt(LocalDateTime.now())
                                .sourceService("user-service")
                                .build();

                ServiceDocument mockDocument = ServiceDocument.builder()
                                .id("test-id")
                                .build();

                when(userConnector.fetchIncremental(lastSync)).thenReturn(List.of(mockResponse));
                when(userConnector.transform(any())).thenReturn(mockDocument);

                // Act
                crawlerService.crawlService(userConnector, false);

                // Assert
                verify(userConnector).fetchIncremental(lastSync);
                verify(userConnector, never()).fetchAll();
                verify(indexerService).bulkIndex(anyList());
        }
}
