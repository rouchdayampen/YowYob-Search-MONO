package com.yowyob.geo.domain.port.out;

import com.yowyob.geo.domain.model.GeoLocation;
import reactor.core.publisher.Mono;

/**
 * Port de sortie — interface vers l'API d'IP Geolocation.
 * Implémenté par {@link com.yowyob.geo.adapter.out.http.IpGeolocationHttpAdapter}.
 */
public interface IpGeolocationPort {

    Mono<GeoLocation> getLocationFromIp(String ipAddress);
}
