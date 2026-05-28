package com.yowyob.search.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.List;

/**
 * Document Elasticsearch pour la collection "crawler-index".
 * Déplacé de document/ vers adapter/out/persistence/ — isolé de la couche domaine.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(indexName = "crawler-index")
public class ProductDocument {
    @Id private String id;
    @Field(name = "title", type = FieldType.Text) private String title;
    @Field(name = "description", type = FieldType.Text) private String description;
    @Field(name = "price", type = FieldType.Double) private Double price;
    @Field(name = "serviceType", type = FieldType.Keyword) private String serviceType;
    @Field(name = "type", type = FieldType.Keyword) private String type;
    @Field(name = "category", type = FieldType.Keyword) private String category;
    @Field(name = "city", type = FieldType.Keyword) private String city;
    @Field(name = "quartier", type = FieldType.Keyword) private String quartier;
    @Field(name = "rating", type = FieldType.Double) private Double rating;
    @GeoPointField @Field(name = "location") private GeoPoint location;
    @Field(name = "latitude", type = FieldType.Double) private Double latitude;
    @Field(name = "longitude", type = FieldType.Double) private Double longitude;
    @Field(name = "images", type = FieldType.Keyword) private List<String> images;
    @Field(name = "imageUrl", type = FieldType.Keyword) private String imageUrl;
    @Field(name = "phone", type = FieldType.Keyword) private String phone;
    @Field(name = "openingHours", type = FieldType.Text) private String openingHours;
    @Field(name = "reviewsCount", type = FieldType.Integer) private Integer reviewsCount;

    public GeoPoint getLocation() {
        if (location == null && latitude != null && longitude != null)
            this.location = new GeoPoint(latitude, longitude);
        return location;
    }
}
