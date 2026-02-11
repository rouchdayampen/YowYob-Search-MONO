package com.yowyob.crawler.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateServiceToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "SERVICE");
        claims.put("service", "crawler-service");
        // Add minimal claims required by AuthFilter in other services
        // Usually email/subject is expected
        claims.put("email", "crawler@yowyob.com");

        return Jwts.builder()
                .claims(claims)
                .subject("crawler-service-id")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
}
