package com.yowyob.geo.domain.port.out;

import com.yowyob.geo.domain.model.GeoLocation;
import com.yowyob.geo.domain.model.RouteResult;
import reactor.core.publisher.Mono;

/**
 * Port de sortie — interface vers l'API Nominatim / OSRM.
 * Implémenté par {@link com.yowyob.geo.adapter.out.http.NominatimHttpAdapter}.
 */
public interface NominatimPort {

    Mono<GeoLocation> geocode(String address);

    Mono<RouteResult> getRoute(double startLat, double startLon, double endLat, double endLon, String mode);
}
