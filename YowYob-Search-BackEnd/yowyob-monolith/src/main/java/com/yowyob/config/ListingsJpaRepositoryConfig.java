package com.yowyob.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.yowyob.listing.repository", entityManagerFactoryRef = "listingsEntityManagerFactory", transactionManagerRef = "listingsTransactionManager")
public class ListingsJpaRepositoryConfig {
}
