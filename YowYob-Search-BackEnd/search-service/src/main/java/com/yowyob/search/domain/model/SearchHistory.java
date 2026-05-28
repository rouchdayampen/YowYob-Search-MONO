package com.yowyob.search.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Modèle de domaine représentant l'historique de recherche.
 * Isolé de l'infrastructure Elasticsearch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    private String id;
    private String userId;
    private String query;
    private String type;
    private String city;
    private Instant timestamp;
}
