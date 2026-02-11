package com.yowyob.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.yowyob.auth.repository", entityManagerFactoryRef = "authEntityManagerFactory", transactionManagerRef = "authTransactionManager")
public class AuthJpaRepositoryConfig {
}
