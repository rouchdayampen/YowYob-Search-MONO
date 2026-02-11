package com.yowyob.crawler.Repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.yowyob.crawler.model.ServiceDocument;

public interface UserRepository extends ElasticsearchRepository<ServiceDocument, Long> {
}