package com.yowyob.auth.config;

import com.yowyob.auth.domain.model.AuthUser;
import com.yowyob.auth.domain.port.out.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initialize system users on application startup
 */
@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeCrawlerUser(AuthUserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String crawlerEmail = "crawler@yowyob.system";
            
            // Check if crawler user already exists
            if (userRepository.existsByEmail(crawlerEmail)) {
                log.info("Crawler user already exists: {}", crawlerEmail);
                return;
            }

            // Create crawler system user
            AuthUser crawlerUser = AuthUser.builder()
                    .name("Yowyob Crawler System")
                    .email(crawlerEmail)
                    .password(passwordEncoder.encode("crawler_secure_password_123"))
                    .role(AuthUser.Role.USER)
                    .status(AuthUser.Status.ACTIVE)
                    .emailVerified(true)
                    .build();

            userRepository.save(crawlerUser);
            log.info("✓ Crawler system user created successfully with ID: {}", crawlerUser.getId());
        };
    }
}
