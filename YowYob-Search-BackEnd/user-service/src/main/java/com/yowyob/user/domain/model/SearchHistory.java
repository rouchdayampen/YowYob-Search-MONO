package com.yowyob.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité SearchHistory du domaine.
 * POJO pur — 0 dépendance JPA / Spring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {

    private UUID id;
    private UUID userId;
    private String query;
    private LocalDateTime searchedAt;
}
