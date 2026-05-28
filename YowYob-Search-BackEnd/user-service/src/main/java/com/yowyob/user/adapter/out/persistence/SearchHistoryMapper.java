package com.yowyob.user.adapter.out.persistence;

import com.yowyob.user.domain.model.SearchHistory;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle de domaine {@link SearchHistory} et l'entité JPA {@link SearchHistoryJpaEntity}.
 */
@Component
public class SearchHistoryMapper {

    public SearchHistory toDomain(SearchHistoryJpaEntity entity) {
        if (entity == null) return null;
        return SearchHistory.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .query(entity.getQuery())
                .searchedAt(entity.getSearchedAt())
                .build();
    }

    public SearchHistoryJpaEntity toEntity(SearchHistory domain) {
        if (domain == null) return null;
        return SearchHistoryJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .query(domain.getQuery())
                .build();
    }
}
