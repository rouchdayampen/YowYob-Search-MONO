package com.yowyob.search.adapter.out.http;

import com.yowyob.search.domain.port.out.GeoLocationPort;
import com.yowyob.search.dto.GeoLocationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Adaptateur HTTP vers le geo-service implémentant {@link GeoLocationPort}.
 */
@Component
@Slf4j
public class GeoServiceHttpAdapter implements GeoLocationPort {

    private final WebClient webClient;

    @Value("${geo.service.url:http://localhost:8085}")
    private String geoServiceUrl;

    public GeoServiceHttpAdapter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<GeoLocationDto> geocode(String city) {
        if (city == null || city.isEmpty()) {
            return Mono.empty();
        }

        log.info("Geocoding city via GeoService: {}", city);

        return webClient.get()
                .uri(geoServiceUrl + "/api/geo/geocode?address={address}", city)
                .retrieve()
                .bodyToMono(GeoLocationDto.class)
                .doOnError(error -> log.error("Failed to geocode city {}: {}", city, error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    @Override
    public Mono<GeoLocationDto> getLocationFromIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return Mono.empty();
        }

        log.info("Getting geolocation for IP via GeoService: {}", ipAddress);

        return webClient.get()
                .uri(geoServiceUrl + "/api/geo/ip-location?ip={ip}", ipAddress)
                .retrieve()
                .bodyToMono(GeoLocationDto.class)
                .doOnError(error -> log.error("Failed to geolocate IP {}: {}", ipAddress, error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }
}
