package com.yowyob.crawler.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service responsible for preventing duplicates from the web crawler.
 * Uses a two-tier approach:
 * 1. Redis Cache: To check if we've seen this exact URL/Hashing recently.
 * 2. Levenshtein Distance: To ensure a new title isn't 90% identical to a scraped one.
 */
@Service
@Slf4j
public class CrawlerDeduplicationService {

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;
    
    @Value("${crawler.deduplication.redis-enabled:true}")
    private boolean redisEnabled;

    private final AtomicInteger redisFailureCount = new AtomicInteger(0);
    
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
            if (isSeenInRedis(urlKey)) {
                log.debug("Listing already seen recently in Redis (URL: {}). Skipping.", listing.getUrl());
                continue;
            }

            // Tier 2: Similarity Match (Levenshtein) against current batch
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
                markAsSeenInRedis(urlKey);
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

    private boolean isSeenInRedis(String key) {
        if (redisTemplate == null || !redisEnabled) return false;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            handleRedisFailure("read", key, e);
            return false;
        }
    }

    private void markAsSeenInRedis(String key) {
        if (redisTemplate == null || !redisEnabled) return;
        try {
            redisTemplate.opsForValue().set(key, "1", 48, TimeUnit.HOURS);
        } catch (Exception e) {
            handleRedisFailure("write", key, e);
        }
    }

    private void handleRedisFailure(String operation, String key, Exception e) {
        int failures = redisFailureCount.incrementAndGet();
        if (failures % 100 == 1) { // log seulement toutes les 100 erreurs
            log.warn("Redis unavailable ({}) — {} cumulative failures. Deduplication degraded. Last error key={}: {}", 
                    operation, failures, key, e.getMessage());
        }
    }
}
