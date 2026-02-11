package com.yowyob.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.yowyob.crawler.Repository")
public class CrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
        System.out.println("=================================");
        System.out.println("Crawler Service started on port 8091");
        System.out.println("=================================");
    }
}