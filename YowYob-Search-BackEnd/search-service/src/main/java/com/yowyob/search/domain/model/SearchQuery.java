package com.yowyob.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value Object encapsulant une requête de recherche.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SearchQuery {
    private String q;
    private String type;
    private String city;
    private String userId;
    private String ipAddress;
}
