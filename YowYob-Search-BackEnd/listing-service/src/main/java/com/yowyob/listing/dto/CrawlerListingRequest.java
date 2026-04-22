package com.yowyob.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerListingRequest {
    private String source;
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
    private Double latitude;
    private Double longitude;
    private LocalDateTime scrapedAt;
}
