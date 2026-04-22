package com.yowyob.crawler.web;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Standardized data model for a listing scraped from the web.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapedListing {
    private String source; // "olx", "jumia", etc.
    private String externalId;
    private String title;
    private String description;
    private Double price;
    private String currency;
    private String city;
    private String country;
    private String category;
    private String url;
    private String imageUrl;
    
    // Geolocation Support for Physical Services
    private Double latitude;
    private Double longitude;
    
    private LocalDateTime scrapedAt;
}
