package com.yowyob.geo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value Object représentant le résultat d'un calcul de distance.
 * POJO pur — 0 dépendance frameworks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceResult {

    private Double distanceKm;
    private Double distanceMiles;
}
