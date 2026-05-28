package com.yowyob.search.domain.port.out;

import com.yowyob.search.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Port de sortie — interface vers l'index Elasticsearch.
 * Implémenté par {@link com.yowyob.search.adapter.out.persistence.ElasticsearchProductAdapter}.
 */
public interface ProductIndexPort {

    Mono<Product> save(Product product);

    Mono<Product> findById(String id);

    Flux<Product> findAll();

    Flux<Product> searchByText(String query);

    Flux<Product> findByTitleOrDescription(String title, String description);
}
