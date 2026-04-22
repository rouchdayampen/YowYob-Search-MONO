package com.yowyob.crawler.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service responsible for preventing duplicates from the web crawler.
 * Uses a two-tier approach:
 * 1. Redis Cache: To check if we've seen this exact URL/Hashing recently.
 * 2. Levenshtein Distance: To ensure a new title isn't 90% identical to a scraped one.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlerDeduplicationService {

    // Ensure you have a RedisTemplate configured in your crawler app
    // Alternatively, you can use StringRedisTemplate
    private final RedisTemplate<String, String> redisTemplate;
    
    private final LevenshteinDistance levenshtein = new LevenshteinDistance();
    private static final double SIMILARITY_THRESHOLD = 0.85;

    /**
     * Filters a list of scraped listings to return only unique, new ones.
     */
    public List<ScrapedListing> filterDuplicates(List<ScrapedListing> rawListings) {
        if (rawListings == null || rawListings.isEmpty()) return new ArrayList<>();

        List<ScrapedListing> uniqueListings = new ArrayList<>();
        
        // We will keep track of titles in this batch to avoid duplicates within the same run
        List<String> currentBatchTitles = new ArrayList<>();

        for (ScrapedListing listing : rawListings) {
            String urlKey = "crawler:seen:url:" + listing.getUrl();
            
            // Tier 1: Exact Match (Redis Check)
            Boolean isSeen = redisTemplate.hasKey(urlKey);
            if (Boolean.TRUE.equals(isSeen)) {
                log.debug("Listing already seen recently in Redis (URL: {}). Skipping.", listing.getUrl());
                continue;
            }

            // Tier 2: Similarity Match (Levenshtein) against current batch
            // Note: For a real production system, you might also compare against actual listings 
            // from the ListingService database or Elasticsearch directly here.
            boolean isTooSimilar = false;
            for (String acceptedTitle : currentBatchTitles) {
                if (calculateSimilarity(listing.getTitle(), acceptedTitle) >= SIMILARITY_THRESHOLD) {
                    isTooSimilar = true;
                    log.debug("Listing '{}' is too similar to '{}'. Skipping.", listing.getTitle(), acceptedTitle);
                    break;
                }
            }

            if (!isTooSimilar) {
                // It's a new, unique listing!
                uniqueListings.add(listing);
                currentBatchTitles.add(listing.getTitle());
                
                // Mark it as seen in Redis for 48 hours to avoid re-scraping it tomorrow
                redisTemplate.opsForValue().set(urlKey, "1", 48, TimeUnit.HOURS);
            }
        }

        log.info("Deduplication complete: {} out of {} listings kept.", uniqueListings.size(), rawListings.size());
        return uniqueListings;
    }

    /**
     * Calculates percentage similarity based on Levenshtein distance.
     * Returns 1.0 for identical strings, 0.0 for completely different.
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;
        
        Integer distance = levenshtein.apply(s1, s2);
        if (distance == null) return 0.0;
        
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        
        return 1.0 - ((double) distance / maxLength);
    }
}
