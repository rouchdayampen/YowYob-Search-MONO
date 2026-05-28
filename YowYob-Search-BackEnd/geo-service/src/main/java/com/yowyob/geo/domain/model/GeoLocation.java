package com.yowyob.geo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value Object représentant une localisation géographique.
 * POJO pur — 0 dépendance frameworks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocation {

    private String address;
    private Double latitude;
    private Double longitude;
    private String city;
    private String country;
}
