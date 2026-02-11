package com.yowyob.crawler.controller;

import com.yowyob.crawler.model.CrawlResult;
import com.yowyob.crawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour le service de Crawler (YowYob Reconfigured).
 * Permet d'interagir avec le crawler via des appels HTTP (déclenchement manuel,
 * test, stats).
 */
@RestController
@RequestMapping("/api/crawler")
@Slf4j
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;

    /**
     * Vérification de l'état du service (Health Check)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "YowYob Crawler (Reconfigured)");
        response.put("version", "1.1.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de test simple pour vérifier que l'API répond.
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("✅ Le service Crawler est en LIGNE et fonctionne ! 🚀");
    }

    /**
     * Lancer un crawl sur une URL spécifique (ex: pour tester l'extraction).
     */
    @PostMapping("/crawl")
    public ResponseEntity<CrawlResult> crawl(@RequestParam String url) {
        log.info("📨 [API] Requête reçue pour crawler l'URL : {}", url);

        try {
            CrawlResult result = crawlerService.crawl(url);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ [API] Erreur lors du crawl de {} : {}", url, e.getMessage());
            CrawlResult errorResult = CrawlResult.builder()
                    .url(url)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();

            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * Déclencher manuellement le crawl complet de TOUS les services connectés.
     * Utile si on veut forcer une synchronisation sans attendre le scheduler.
     */
    @PostMapping("/run")
    public ResponseEntity<String> runCrawl(@RequestParam(defaultValue = "false") boolean force) {
        log.info("🚀 [API] Ordre de crawl MANUEL reçu (Force={}) ! Lancement du processus...", force);
        crawlerService.crawlAllServices(force);
        return ResponseEntity.ok("✅ Commande reçue : Le crawl des services a démarré en arrière-plan.");
    }

    /**
     * Obtenir des statistiques basiques sur le crawler.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "Endpoint de statistiques");
        stats.put("note", "TODO: Implémenter des métriques détaillées (nb documents indexés, erreurs, etc.)");
        return ResponseEntity.ok(stats);
    }
}
