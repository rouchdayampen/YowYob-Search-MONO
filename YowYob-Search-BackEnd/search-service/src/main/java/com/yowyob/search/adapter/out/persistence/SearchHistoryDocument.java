package com.yowyob.search.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/**
 * Entité Elasticsearch pour l'historique de recherche.
 * Isolée dans la couche adaptateur.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "search_history")
public class SearchHistoryDocument {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Text)
    private String query;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Date)
    private Instant timestamp;
}
