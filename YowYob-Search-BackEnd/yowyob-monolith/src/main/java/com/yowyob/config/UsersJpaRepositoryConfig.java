package com.yowyob.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.yowyob.user.repository", entityManagerFactoryRef = "usersEntityManagerFactory", transactionManagerRef = "usersTransactionManager")
public class UsersJpaRepositoryConfig {
}
