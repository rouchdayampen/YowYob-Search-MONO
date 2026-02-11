package com.yowyob.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JpaConfig {

    private static Map<String, Object> jpaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", false);
        props.put("hibernate.format_sql", true);
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        return props;
    }

    // ==================== Auth Datasource (Primary) ====================

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties authDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource authDataSource() {
        return authDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean authEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(authDataSource())
                .packages("com.yowyob.auth.entity")
                .persistenceUnit("auth")
                .properties(jpaProperties())
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager authTransactionManager(
            @Qualifier("authEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    // ==================== Users Datasource ====================

    @Bean
    @ConfigurationProperties("app.datasource.users")
    public DataSourceProperties usersDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource usersDataSource() {
        return usersDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean usersEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(usersDataSource())
                .packages("com.yowyob.user.entity")
                .persistenceUnit("users")
                .properties(jpaProperties())
                .build();
    }

    @Bean
    public PlatformTransactionManager usersTransactionManager(
            @Qualifier("usersEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    // ==================== Listings Datasource ====================

    @Bean
    @ConfigurationProperties("app.datasource.listings")
    public DataSourceProperties listingsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource listingsDataSource() {
        return listingsDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean listingsEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(listingsDataSource())
                .packages("com.yowyob.listing.entity")
                .persistenceUnit("listings")
                .properties(jpaProperties())
                .build();
    }

    @Bean
    public PlatformTransactionManager listingsTransactionManager(
            @Qualifier("listingsEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
