package com.yowyob.geo.adapter.out.http;

import com.yowyob.geo.domain.model.GeoLocation;
import com.yowyob.geo.domain.port.out.IpGeolocationPort;
import com.yowyob.geo.dto.GeoLocationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Adaptateur HTTP implémentant {@link IpGeolocationPort}.
 * Encapsule l'API ip-api.com et Redis — le domaine ne les connaît pas.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IpGeolocationHttpAdapter implements IpGeolocationPort {

    private final WebClient webClient;

    @Qualifier("reactiveRedisTemplateForGeoLocation")
    private final ReactiveRedisTemplate<String, GeoLocationDto> redisTemplate;

    @Value("${app.ipapi.url:http://ip-api.com}")
    private String ipapiUrl;

    private static final GeoLocation DEFAULT_LOCATION = GeoLocation.builder()
            .city("Douala").country("Cameroon").latitude(4.0511).longitude(9.7679).build();

    @Override
    public Mono<GeoLocation> getLocationFromIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty() || ipAddress.equals("127.0.0.1")) {
            return Mono.just(DEFAULT_LOCATION);
        }
        String key = "geo:ip:" + ipAddress;
        return redisTemplate.opsForValue().get(key)
                .map(dto -> GeoLocation.builder().city(dto.getCity()).country(dto.getCountry())
                        .latitude(dto.getLatitude()).longitude(dto.getLongitude()).build())
                .switchIfEmpty(fetchFromIpApi(ipAddress).flatMap(loc -> {
                    GeoLocationDto dto = GeoLocationDto.builder().city(loc.getCity())
                            .country(loc.getCountry()).latitude(loc.getLatitude())
                            .longitude(loc.getLongitude()).build();
                    return redisTemplate.opsForValue().set(key, dto, Duration.ofDays(30)).thenReturn(loc);
                }))
                .onErrorReturn(DEFAULT_LOCATION);
    }

    @SuppressWarnings("unchecked")
    private Mono<GeoLocation> fetchFromIpApi(String ip) {
        return webClient.get().uri(ipapiUrl + "/json/{ip}", ip)
                .header("User-Agent", "YowYob-Search-Service").retrieve()
                .bodyToMono(Map.class).map(r -> {
                    Double lat = r.get("lat") != null ? Double.parseDouble(r.get("lat").toString()) : null;
                    Double lon = r.get("lon") != null ? Double.parseDouble(r.get("lon").toString()) : null;
                    return GeoLocation.builder().city((String) r.get("city"))
                            .country((String) r.get("country")).latitude(lat).longitude(lon).build();
                })
                .onErrorReturn(DEFAULT_LOCATION);
    }
}
