package com.yowyob.geo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Value Object représentant un itinéraire calculé.
 * POJO pur — 0 dépendance frameworks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResult {

    private Double distance;   // en mètres
    private Double duration;   // en secondes
    private String polyline;   // JSON de la liste de points [[lat,lon], ...]
}
