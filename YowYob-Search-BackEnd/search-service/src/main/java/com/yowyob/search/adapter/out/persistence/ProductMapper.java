package com.yowyob.search.adapter.out.persistence;

import com.yowyob.search.domain.model.Product;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle de domaine {@link Product} et le document Elasticsearch {@link ProductDocument}.
 */
@Component
public class ProductMapper {

    public Product toDomain(ProductDocument doc) {
        if (doc == null) return null;
        return Product.builder()
                .id(doc.getId()).title(doc.getTitle()).description(doc.getDescription())
                .price(doc.getPrice()).serviceType(doc.getServiceType()).type(doc.getType())
                .category(doc.getCategory()).city(doc.getCity()).quartier(doc.getQuartier())
                .rating(doc.getRating()).latitude(doc.getLatitude()).longitude(doc.getLongitude())
                .images(doc.getImages()).imageUrl(doc.getImageUrl()).phone(doc.getPhone())
                .openingHours(doc.getOpeningHours()).reviewsCount(doc.getReviewsCount()).build();
    }

    public ProductDocument toDocument(Product p) {
        if (p == null) return null;
        return ProductDocument.builder()
                .id(p.getId()).title(p.getTitle()).description(p.getDescription())
                .price(p.getPrice()).serviceType(p.getServiceType()).type(p.getType())
                .category(p.getCategory()).city(p.getCity()).quartier(p.getQuartier())
                .rating(p.getRating()).latitude(p.getLatitude()).longitude(p.getLongitude())
                .images(p.getImages()).imageUrl(p.getImageUrl()).phone(p.getPhone())
                .openingHours(p.getOpeningHours()).reviewsCount(p.getReviewsCount()).build();
    }
}
