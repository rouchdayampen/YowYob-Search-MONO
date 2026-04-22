package com.yowyob.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceDocument {

    private String id; // <-- id Elasticsearch (serviceType_serviceId)

    private String serviceType; // "user", "voyage", "hotel", etc.
    private String serviceId; // ID unique du document dans le service
    private String title;
    private String description;

    // Champs specifiques aux utilisateurs
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;

    // Champs specifiques aux listings (Produits/Services)
    private Double price;
    private String category;
    private String location;
    private String image;

    // Champs communs
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime indexedAt;

    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;
}
