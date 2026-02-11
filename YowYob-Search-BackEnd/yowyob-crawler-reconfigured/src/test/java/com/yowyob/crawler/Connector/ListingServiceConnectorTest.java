package com.yowyob.crawler.Connector;

import com.yowyob.crawler.config.JwtTokenProvider;
import com.yowyob.crawler.model.ListingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ListingServiceConnectorTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private ListingServiceConnector listingServiceConnector;

    @BeforeEach
    void setUp() {
        listingServiceConnector = new ListingServiceConnector(restTemplate, jwtTokenProvider);
        ReflectionTestUtils.setField(listingServiceConnector, "baseUrl", "http://localhost:8083/api/listings");
        ReflectionTestUtils.setField(listingServiceConnector, "enabled", true);
    }

    @Test
    void fetchIncremental_ShouldUseUpdatedAfterParam() {
        // Arrange
        LocalDateTime lastSync = LocalDateTime.now().minusHours(2);
        String expectedToken = "service-token";
        when(jwtTokenProvider.generateServiceToken()).thenReturn(expectedToken);

        ListingResponse[] mockListings = new ListingResponse[0];
        ResponseEntity<ListingResponse[]> responseEntity = new ResponseEntity<>(mockListings, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ListingResponse[].class))).thenReturn(responseEntity);

        // Act
        listingServiceConnector.fetchIncremental(lastSync);

        // Assert
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
                eq(ListingResponse[].class));

        // Verify URL contains updatedAfter param
        String url = urlCaptor.getValue();
        assertTrue(url.contains("/api/listing/search/documents"));
        assertTrue(url.contains("updatedAfter="));
        assertTrue(url.contains(lastSync.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        // Verify Header
        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertEquals("Bearer " + expectedToken, headers.getFirst("Authorization"));
    }
}
