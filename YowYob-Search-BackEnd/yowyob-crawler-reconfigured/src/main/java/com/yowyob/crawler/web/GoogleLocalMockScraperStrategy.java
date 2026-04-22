package com.yowyob.crawler.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class GoogleLocalMockScraperStrategy implements WebScraperStrategy {

    public static final String STRATEGY_NAME = "google-local-mock";

    @Value("${scraper.googlelocalmock.enabled:false}")
    private boolean enabled;

    @Override
    public String getSourceName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean isEnabled() {
        return enabled; 
    }

    @Override
    public List<ScrapedListing> scrape() {
        log.info("[E2E] GoogleLocalMockScraperStrategy — injecting controlled test fixtures");

        List<ScrapedListing> fixtures = List.of(
            ScrapedListing.builder()
                .source(STRATEGY_NAME)
                .externalId("mock-tchokos-sarl-001")
                .title("Tchokos SARL")
                .category("Magasin de chaussures")
                .city("Douala")
                .phone("6 91 98 10 47")
                .openingHours("Fermé - Ouvre à 08:00")
                .rating(3.7)
                .reviewsCount(3)
                .description("Pour une première fois j'ai été satisfait.")
                .imageUrl("https://via.placeholder.com/150?text=Tchokos+SARL")
                .url("https://mock.yowyob.local/listing/tchokos-sarl")
                .build(),

            ScrapedListing.builder()
                .source(STRATEGY_NAME)
                .externalId("mock-tchokos-express-002")
                .title("TCHOKOS SERVICE EXPRESS")
                .category("Magasin de gros")
                .city("Douala")
                .phone("6 91 89 89 76")
                .openingHours("Fermé - Ouvre à 08:00")
                .rating(5.0)
                .reviewsCount(2)
                .description("Service rapide et professionnel.")
                .imageUrl("https://via.placeholder.com/150?text=Tchokos+Express")
                .url("https://mock.yowyob.local/listing/tchokos-express")
                .build()
        );

        log.info("[E2E] {} mock listings generated", fixtures.size());
        return fixtures;
    }
}
