package com.yowyob.crawler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.crawler.dto.WikimediaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@RequiredArgsConstructor
public class WikimediaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String WIKIMEDIA_URL =
        "https://commons.wikimedia.org/w/api.php";

    // Délai entre requêtes pour respecter le rate limit Wikimedia (1 req/s)
    private static final long RATE_LIMIT_DELAY_MS = 1000;

    /**
     * Stratégie hybride :
     * 1. Cherche par nom du commerce
     * 2. Si rien → cherche par coordonnées GPS (rayon 100m)
     * 3. Si rien → retourne null (acceptable)
     */
    public String findPhoto(String name, double lat, double lng) {
        String photoUrl = searchByName(name);
        if (photoUrl != null) {
            log.debug("Photo trouvée par nom pour : {}", name);
            return photoUrl;
        }

        photoUrl = searchByGps(lat, lng);
        if (photoUrl != null) {
            log.debug("Photo trouvée par GPS pour : {}", name);
            return photoUrl;
        }

        log.debug("Aucune photo trouvée pour : {}", name);
        return null;
    }

    // ── Méthodes privées ──────────────────────────────────────────

    private void sleep() {
        try { Thread.sleep(RATE_LIMIT_DELAY_MS); } catch (InterruptedException ignored) {}
    }

    private HttpHeaders wikimediaHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "YowYob-Crawler/1.0 (contact@yowyob.com)");
        return headers;
    }

    private String searchByName(String name) {
        try {
            sleep();
            String url = UriComponentsBuilder.fromHttpUrl(WIKIMEDIA_URL)
                .queryParam("action", "query")
                .queryParam("list", "search")
                .queryParam("srsearch", name)
                .queryParam("srnamespace", "6")
                .queryParam("srlimit", "1")
                .queryParam("format", "json")
                .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(wikimediaHeaders()), String.class);

            WikimediaResponse parsed = objectMapper.readValue(
                response.getBody(), WikimediaResponse.class);

            if (parsed.getQuery() == null
                    || parsed.getQuery().getSearch() == null
                    || parsed.getQuery().getSearch().isEmpty()) {
                return null;
            }

            String title = parsed.getQuery().getSearch().get(0).getTitle();
            return fetchImageUrl(title);

        } catch (Exception e) {
            log.warn("Erreur Wikimedia nom '{}' : {}", name, e.getMessage());
            return null;
        }
    }

    private String searchByGps(double lat, double lng) {
        try {
            sleep();
            String url = UriComponentsBuilder.fromHttpUrl(WIKIMEDIA_URL)
                .queryParam("action", "query")
                .queryParam("generator", "geosearch")
                .queryParam("ggscoord", lat + "|" + lng)
                .queryParam("ggsradius", "100")
                .queryParam("ggslimit", "1")
                .queryParam("prop", "imageinfo")
                .queryParam("iiprop", "url")
                .queryParam("format", "json")
                .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(wikimediaHeaders()), String.class);

            WikimediaResponse parsed = objectMapper.readValue(
                response.getBody(), WikimediaResponse.class);

            if (parsed.getQuery() == null
                    || parsed.getQuery().getPages() == null
                    || parsed.getQuery().getPages().isEmpty()) {
                return null;
            }

            return parsed.getQuery().getPages().values().stream()
                .filter(p -> p.getImageinfo() != null && !p.getImageinfo().isEmpty())
                .map(p -> p.getImageinfo().get(0).getUrl())
                .filter(this::isValidImageUrl)
                .findFirst()
                .orElse(null);

        } catch (Exception e) {
            log.warn("Erreur Wikimedia GPS ({},{}) : {}", lat, lng, e.getMessage());
            return null;
        }
    }

    private String fetchImageUrl(String title) {
        try {
            sleep();
            String url = UriComponentsBuilder.fromHttpUrl(WIKIMEDIA_URL)
                .queryParam("action", "query")
                .queryParam("titles", title)
                .queryParam("prop", "imageinfo")
                .queryParam("iiprop", "url")
                .queryParam("format", "json")
                .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(wikimediaHeaders()), String.class);

            WikimediaResponse parsed = objectMapper.readValue(
                response.getBody(), WikimediaResponse.class);

            if (parsed.getQuery() == null || parsed.getQuery().getPages() == null) {
                return null;
            }

            return parsed.getQuery().getPages().values().stream()
                .filter(p -> p.getImageinfo() != null && !p.getImageinfo().isEmpty())
                .map(p -> p.getImageinfo().get(0).getUrl())
                .filter(this::isValidImageUrl)
                .findFirst()
                .orElse(null);

        } catch (Exception e) {
            log.warn("Erreur fetchImageUrl '{}' : {}", title, e.getMessage());
            return null;
        }
    }

    private boolean isValidImageUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.endsWith(".jpg")
            || lower.endsWith(".jpeg")
            || lower.endsWith(".png")
            || lower.endsWith(".webp");
    }
}
