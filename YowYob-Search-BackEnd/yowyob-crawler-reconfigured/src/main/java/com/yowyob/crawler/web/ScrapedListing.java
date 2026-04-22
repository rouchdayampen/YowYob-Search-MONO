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
    
    // Rich Local Business Data
    private String phone;           // tag OSM : phone ou contact:phone
    private String openingHours;    // tag OSM : opening_hours
    private Double rating;          // null par défaut — jamais généré aléatoirement
    private Integer reviewsCount;   // null par défaut — jamais généré aléatoirement
    
    private LocalDateTime scrapedAt;
}
