package com.yowyob.search.adapter.out.persistence;

import com.yowyob.search.domain.model.Product;
import com.yowyob.search.domain.port.out.ProductIndexPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptateur de persistance Elasticsearch implémentant {@link ProductIndexPort}.
 */
@Component
@RequiredArgsConstructor
public class ElasticsearchProductAdapter implements ProductIndexPort {

    private final ProductSearchRepository repository;
    private final ProductMapper mapper;

    @Override
    public Mono<Product> save(Product product) {
        ProductDocument doc = mapper.toDocument(product);
        return repository.save(doc).map(mapper::toDomain);
    }

    @Override
    public Mono<Product> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<Product> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Flux<Product> searchByText(String query) {
        // Pour l'instant on utilise findByTitleContainingOrDescriptionContaining
        return repository.findByTitleContainingOrDescriptionContaining(query, query)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Product> findByTitleOrDescription(String title, String description) {
        return repository.findByTitleContainingOrDescriptionContaining(title, description)
                .map(mapper::toDomain);
    }
}
