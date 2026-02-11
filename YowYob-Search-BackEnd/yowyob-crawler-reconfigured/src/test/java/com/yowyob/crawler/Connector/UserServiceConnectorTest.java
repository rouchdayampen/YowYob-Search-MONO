package com.yowyob.crawler.Connector;

import com.yowyob.crawler.config.JwtTokenProvider;
import com.yowyob.crawler.model.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceConnectorTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private UserServiceConnector userServiceConnector;

    @BeforeEach
    void setUp() {
        userServiceConnector = new UserServiceConnector(restTemplate, jwtTokenProvider);
        ReflectionTestUtils.setField(userServiceConnector, "baseUrl", "http://localhost:8084/api/users");
        ReflectionTestUtils.setField(userServiceConnector, "enabled", true);
    }

    @Test
    void fetchAll_ShouldUseCorrectEndpointAndHeader() {
        // Arrange
        String expectedToken = "test-token";
        when(jwtTokenProvider.generateServiceToken()).thenReturn(expectedToken);

        UserResponse[] mockUsers = new UserResponse[0];
        ResponseEntity<UserResponse[]> responseEntity = new ResponseEntity<>(mockUsers, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8084/api/user/search/documents"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserResponse[].class))).thenReturn(responseEntity);

        // Act
        userServiceConnector.fetchAll();

        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), any(), entityCaptor.capture(), eq(UserResponse[].class));

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertEquals("Bearer " + expectedToken, headers.getFirst("Authorization"));
    }
}
