package com.yowyob.crawler.web;

import java.util.List;

/**
 * Strategy interface for web scrapers.
 * Each supported website (OLX, Jumia, etc.) will have its own implementation of this.
 */
public interface WebScraperStrategy {
    
    /**
     * Executes the scraping process for the target website.
     * @return A list of successfully scraped and normalized listings.
     */
    List<ScrapedListing> scrape();

    /**
     * Name of the source (e.g., "olx", "jumia")
     */
    String getSourceName();
    
    /**
     * Is this strategy currently enabled via properties?
     */
    boolean isEnabled();
}
