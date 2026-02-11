package com.yowyob.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResult {
    private String url;
    private boolean success;
    private String errorMessage;
    private Integer httpStatusCode;
    private LocalDateTime crawledAt;
    private Long durationMs;
    
    // Données extraites
    private Map<String, Object> extractedData;
    private Integer itemsFound;
    private List<String> discoveredLinks;
    
    // Métadonnées
    private String contentHash;
    private Long pageSize;
    private String detectedLanguage;
    private String extractorUsed;
}
