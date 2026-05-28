package com.yowyob.geo.domain.port.in;

import com.yowyob.geo.domain.model.DistanceResult;
import com.yowyob.geo.domain.model.GeoLocation;
import com.yowyob.geo.domain.model.RouteResult;
import reactor.core.publisher.Mono;

/**
 * Port d'entrée (Use Case) — définit les opérations de géolocalisation.
 * Implémenté par {@link com.yowyob.geo.domain.service.GeoApplicationService}.
 */
public interface GeoUseCase {

    Mono<GeoLocation> geocode(String address);

    DistanceResult calculateDistance(double lat1, double lon1, double lat2, double lon2);

    Mono<RouteResult> getRoute(double startLat, double startLon, double endLat, double endLon, String mode);

    Mono<GeoLocation> getLocationFromIp(String ipAddress);
}
