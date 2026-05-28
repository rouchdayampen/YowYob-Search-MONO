package com.yowyob.geo.domain.service;

import com.yowyob.geo.domain.model.DistanceResult;
import com.yowyob.geo.domain.model.GeoLocation;
import com.yowyob.geo.domain.model.RouteResult;
import com.yowyob.geo.domain.port.in.GeoUseCase;
import com.yowyob.geo.domain.port.out.IpGeolocationPort;
import com.yowyob.geo.domain.port.out.NominatimPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service applicatif du domaine Geo.
 * Implémente {@link GeoUseCase}.
 * Dépend uniquement des ports de sortie — pas de WebClient, pas de Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeoApplicationService implements GeoUseCase {

    private final NominatimPort nominatimPort;
    private final IpGeolocationPort ipGeolocationPort;

    @Override
    public Mono<GeoLocation> geocode(String address) {
        return nominatimPort.geocode(address);
    }

    @Override
    public DistanceResult calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double distanceKm = haversine(lat1, lon1, lat2, lon2);
        return DistanceResult.builder()
                .distanceKm(distanceKm)
                .distanceMiles(distanceKm * 0.621371)
                .build();
    }

    @Override
    public Mono<RouteResult> getRoute(double startLat, double startLon, double endLat, double endLon, String mode) {
        return nominatimPort.getRoute(startLat, startLon, endLat, endLon, mode);
    }

    @Override
    public Mono<GeoLocation> getLocationFromIp(String ipAddress) {
        return ipGeolocationPort.getLocationFromIp(ipAddress);
    }

    /**
     * Formule de Haversine — calcul de distance entre deux points GPS.
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon terrestre en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
