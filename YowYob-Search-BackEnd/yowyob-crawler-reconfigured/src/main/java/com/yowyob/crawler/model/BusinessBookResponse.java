package com.yowyob.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessBookResponse {
    private String service;
    private LocalDateTime timestamp;
    private List<BusinessBookDocument> documents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BusinessBookDocument {
        private String id;
        private String entity;
        private String title;
        private String description;
        private Map<String, Object> content;
        private List<String> tags;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
