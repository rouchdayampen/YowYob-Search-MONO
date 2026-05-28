package com.yowyob.search.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SearchHistoryRepository extends ReactiveElasticsearchRepository<SearchHistoryDocument, String> {
    Flux<SearchHistoryDocument> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);
}
