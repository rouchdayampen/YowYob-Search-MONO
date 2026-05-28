package com.yowyob.geo.adapter.out.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.geo.domain.model.GeoLocation;
import com.yowyob.geo.domain.model.RouteResult;
import com.yowyob.geo.domain.port.out.NominatimPort;
import com.yowyob.geo.dto.GeocodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adaptateur HTTP implémentant {@link NominatimPort}.
 * Encapsule Nominatim, OSRM et Redis.
 */
@Component
@Slf4j
public class NominatimHttpAdapter implements NominatimPort {

    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, GeocodeResponse> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String OSRM_BASE = "http://router.project-osrm.org/route/v1";

    @Value("${app.nominatim.url}")
    private String nominatimUrl;

    @Value("${app.nominatim.user-agent}")
    private String userAgent;

    public NominatimHttpAdapter(WebClient.Builder builder,
                                ReactiveRedisTemplate<String, GeocodeResponse> redisTemplate) {
        this.webClient = builder.build();
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<GeoLocation> geocode(String address) {
        String key = "geo:geocode:" + address.toLowerCase().trim().replaceAll("\\s+", "_");
        return redisTemplate.opsForValue().get(key)
                .map(c -> GeoLocation.builder().address(c.getAddress())
                        .latitude(c.getLatitude()).longitude(c.getLongitude()).build())
                .switchIfEmpty(fetchNominatim(address).flatMap(loc -> {
                    GeocodeResponse c = GeocodeResponse.builder().address(loc.getAddress())
                            .latitude(loc.getLatitude()).longitude(loc.getLongitude()).build();
                    return redisTemplate.opsForValue().set(key, c, Duration.ofDays(30)).thenReturn(loc);
                }));
    }

    @SuppressWarnings("unchecked")
    private Mono<GeoLocation> fetchNominatim(String address) {
        return webClient.get()
                .uri(nominatimUrl + "/search?format=json&q=" + address + "&limit=1")
                .header("User-Agent", userAgent).retrieve().bodyToMono(List.class)
                .flatMap(list -> {
                    if (list.isEmpty()) return Mono.empty();
                    Map<String, Object> r = (Map<String, Object>) list.get(0);
                    return Mono.just(GeoLocation.builder()
                            .address((String) r.get("display_name"))
                            .latitude(Double.parseDouble((String) r.get("lat")))
                            .longitude(Double.parseDouble((String) r.get("lon"))).build());
                })
                .onErrorResume(e -> { log.error("Nominatim error: {}", e.getMessage()); return Mono.empty(); });
    }

    @Override
    public Mono<RouteResult> getRoute(double sLat, double sLon, double eLat, double eLon, String mode) {
        String m = mode == null ? "driving" : switch (mode.toLowerCase()) {
            case "bike", "cycling" -> "cycling";
            case "foot", "walking" -> "walking";
            default -> "driving";
        };
        String uri = String.format("%s/%s/%f,%f;%f,%f?overview=full&geometries=geojson",
                OSRM_BASE, m, sLon, sLat, eLon, eLat);
        return webClient.get().uri(uri).retrieve().bodyToMono(String.class).map(json -> {
            try {
                JsonNode route = objectMapper.readTree(json).path("routes").get(0);
                JsonNode geo = route.path("geometry").path("coordinates");
                List<List<Double>> pts = new ArrayList<>();
                if (geo.isArray()) for (JsonNode p : geo) pts.add(List.of(p.get(1).asDouble(), p.get(0).asDouble()));
                return RouteResult.builder().distance(route.path("distance").asDouble())
                        .duration(route.path("duration").asDouble())
                        .polyline(objectMapper.writeValueAsString(pts)).build();
            } catch (Exception e) { throw new RuntimeException("OSRM parse error", e); }
        }).onErrorResume(e -> {
            log.error("Route error: {}", e.getMessage());
            try {
                return Mono.just(RouteResult.builder()
                        .distance(haversine(sLat, sLon, eLat, eLon) * 1000).duration(0.0)
                        .polyline(objectMapper.writeValueAsString(List.of(List.of(sLat, sLon), List.of(eLat, eLon)))).build());
            } catch (Exception ex) { return Mono.empty(); }
        });
    }

    private double haversine(double la1, double lo1, double la2, double lo2) {
        double dLat = Math.toRadians(la2 - la1), dLon = Math.toRadians(lo2 - lo1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(Math.toRadians(la1))*Math.cos(Math.toRadians(la2))*Math.sin(dLon/2)*Math.sin(dLon/2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
}
