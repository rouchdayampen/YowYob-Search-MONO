package com.yowyob.crawler.web;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Declarative HTTP client for injecting scraped listings into the Listing Service.
 * NOTE: The base URL is NOT set here via annotation (Spring Boot 4 does not resolve
 * ${...} placeholders in @HttpExchange(url=...)). It is set programmatically
 * in CrawlerApplication via RestClient.builder().baseUrl(...).
 */
@HttpExchange
public interface ListingServiceClient {

    @PostExchange("/crawler/ingest")
    void ingestListing(@RequestBody ScrapedListing listing);
}
