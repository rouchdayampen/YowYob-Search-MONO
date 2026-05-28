package com.yowyob.geo.adapter.in.rest;

import com.yowyob.geo.domain.model.DistanceResult;
import com.yowyob.geo.domain.model.GeoLocation;
import com.yowyob.geo.domain.model.RouteResult;
import com.yowyob.geo.domain.port.in.GeoUseCase;
import com.yowyob.geo.dto.DistanceRequest;
import com.yowyob.geo.dto.DistanceResponse;
import com.yowyob.geo.dto.GeocodeResponse;
import com.yowyob.geo.dto.GeoLocationDto;
import com.yowyob.geo.dto.RouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Adaptateur REST entrant pour le domaine Geo.
 * Remplace l'ancien {@code GeoController}.
 * Dépend uniquement du port d'entrée {@link GeoUseCase}.
 */
@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoRestAdapter {

    private final GeoUseCase geoUseCase;

    @GetMapping("/geocode")
    public Mono<ResponseEntity<GeocodeResponse>> geocode(@RequestParam String address) {
        return geoUseCase.geocode(address)
                .map(loc -> ResponseEntity.ok(GeocodeResponse.builder()
                        .address(loc.getAddress())
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/distance")
    public ResponseEntity<DistanceResponse> calculateDistance(@RequestBody DistanceRequest request) {
        DistanceResult result = geoUseCase.calculateDistance(
                request.getLat1(), request.getLon1(),
                request.getLat2(), request.getLon2());
        return ResponseEntity.ok(new DistanceResponse(result.getDistanceKm(), result.getDistanceMiles()));
    }

    @GetMapping("/ip-location")
    public Mono<ResponseEntity<GeoLocationDto>> getIpLocation(
            @RequestParam(required = false) String ip,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
            @RequestHeader(value = "Remote-Addr", required = false) String remoteAddr) {

        String targetIp = ip;
        if (targetIp == null || targetIp.isEmpty()) {
            targetIp = xForwardedFor != null ? xForwardedFor.split(",")[0].trim() : remoteAddr;
        }
        if (targetIp == null || targetIp.equals("0:0:0:0:0:0:0:1") || targetIp.equals("127.0.0.1")) {
            targetIp = "127.0.0.1";
        }

        String finalIp = targetIp;
        return geoUseCase.getLocationFromIp(finalIp)
                .map(loc -> ResponseEntity.ok(GeoLocationDto.builder()
                        .city(loc.getCity())
                        .country(loc.getCountry())
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .build()))
                .doOnError(e -> System.err.println("Error for IP " + finalIp + ": " + e.getMessage()));
    }

    @GetMapping("/route")
    public Mono<ResponseEntity<RouteResponse>> getRoute(
            @RequestParam double startLat, @RequestParam double startLon,
            @RequestParam double endLat, @RequestParam double endLon,
            @RequestParam(required = false, defaultValue = "driving") String mode) {
        return geoUseCase.getRoute(startLat, startLon, endLat, endLon, mode)
                .map(r -> ResponseEntity.ok(RouteResponse.builder()
                        .distance(r.getDistance())
                        .duration(r.getDuration())
                        .polyline(r.getPolyline())
                        .build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public String health() {
        return "Geo Service is running!";
    }
}
