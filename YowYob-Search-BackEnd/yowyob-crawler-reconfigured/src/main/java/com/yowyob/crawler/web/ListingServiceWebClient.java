package com.yowyob.crawler.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for injecting the cleaned web data into the YowYob system.
 * It uses Option A from the architectural design: POSTing data directly to the
 * Listing Service API. This ensures the data goes through the standard domain logic,
 * generates Kafka events naturally, and eventually gets indexed in Elasticsearch.
 */
@Service
@Slf4j
public class ListingServiceWebClient {

    private final ListingServiceClient listingServiceClient;

    public ListingServiceWebClient(ListingServiceClient listingServiceClient) {
        this.listingServiceClient = listingServiceClient;
    }

    /**
     * Injects a batch of unique scraped listings into the system.
     */
    public void injectListings(List<ScrapedListing> uniqueListings) {
        if (uniqueListings == null || uniqueListings.isEmpty()) return;

        log.info("Injecting {} unique web listings into Listing Service API", uniqueListings.size());

        int successCount = 0;
        
        for (ScrapedListing listing : uniqueListings) {
            try {
                // POST to listing service via Spring Boot 4 declarative HTTP client
                listingServiceClient.ingestListing(listing);
                successCount++;
                
            } catch (Exception e) {
                log.error("Failed to inject scraped listing {} into the system: {}", listing.getTitle(), e.getMessage());
                // We keep going, one bad listing shouldn't block the ingestion of others
            }
        }
        
        log.info("Completed injection. {}/{} listings successfully posted.", successCount, uniqueListings.size());
    }
}
