package com.yowyob.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Modèle de domaine Product — POJO pur, sans annotations Elasticsearch.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Product {
    private String id;
    private String title;
    private String description;
    private Double price;
    private String serviceType;
    private String type;
    private String category;
    private String city;
    private String quartier;
    private Double rating;
    private Double latitude;
    private Double longitude;
    private List<String> images;
    private String imageUrl;
    private String phone;
    private String openingHours;
    private Integer reviewsCount;
}
