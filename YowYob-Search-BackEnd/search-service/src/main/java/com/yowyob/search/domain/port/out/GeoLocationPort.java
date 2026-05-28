package com.yowyob.search.domain.port.out;

import com.yowyob.search.dto.GeoLocationDto;
import reactor.core.publisher.Mono;

/**
 * Port de sortie — interface vers le service de géolocalisation.
 * Implémenté par {@link com.yowyob.search.adapter.out.http.GeoServiceHttpAdapter}.
 */
public interface GeoLocationPort {

    Mono<GeoLocationDto> geocode(String city);

    Mono<GeoLocationDto> getLocationFromIp(String ipAddress);
}
