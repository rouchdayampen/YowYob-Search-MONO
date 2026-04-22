package com.yowyob.crawler.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The main orchestrator for Web Crawling.
 * It loops through all activated strategies, collects scraped listings,
 * and passes them to the deduplication and injection services.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WebCrawlerModule {

    // Spring will automatically inject all beans implementing WebScraperStrategy!
    private final List<WebScraperStrategy> scrapers;
    private final CrawlerDeduplicationService deduplicationService;
    private final ListingServiceWebClient listingServiceWebClient;

    /**
     * Executes the web scraping jobs automatically.
     * Modified for demonstration to run 5 seconds after startup, then every hour.
     */
    @Scheduled(initialDelay = 5000, fixedDelay = 3600000)
    public void runScrapingJobs() {
        log.info("--- Starting Hybrid Web Crawler Batch ---");

        if (scrapers == null || scrapers.isEmpty()) {
            log.warn("No WebScraperStrategy implementations found or enabled.");
            return;
        }

        List<ScrapedListing> allListings = new ArrayList<>();

        for (WebScraperStrategy scraper : scrapers) {
            if (scraper.isEnabled()) {
                log.info("Executing scraper for source: {}", scraper.getSourceName());
                try {
                    List<ScrapedListing> results = scraper.scrape();
                    log.info("Scraper {} found {} listings.", scraper.getSourceName(), results.size());
                    allListings.addAll(results);
                } catch (Exception e) {
                    log.error("Scraper {} failed entirely: {}", scraper.getSourceName(), e.getMessage(), e);
                }
            } else {
                log.debug("Scraper {} is disabled.", scraper.getSourceName());
            }
        }

        log.info("Total listings scraped from all sources: {}", allListings.size());

        // --- Phase 3 & 4: Deduplicate & Inject ---
        List<ScrapedListing> uniqueListings = deduplicationService.filterDuplicates(allListings);
        listingServiceWebClient.injectListings(uniqueListings);
    }
}
