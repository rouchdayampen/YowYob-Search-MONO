package com.yowyob.crawler.service;

import com.yowyob.auth.event.UserCreatedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import com.yowyob.crawler.Connector.ServiceConnector;
import com.yowyob.crawler.model.ConnectorResponse;
import com.yowyob.crawler.model.ServiceDocument;
import com.yowyob.crawler.model.CrawlResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service Principal du Crawler (Reconfiguré).
 * 
 * Ce service est le coeur du système d'indexation. Il a 3 responsabilités
 * principales :
 * 1. Écouter les événements Kafka (ex: création d'un utilisateur) pour indexer
 * en temps réel.
 * 2. Lancer des tâches planifiées (Scheduled) pour récupérer les données des
 * autres microservices (User, Listing, etc.).
 * 3. Permettre un crawl manuel ou via API.
 */
@Service
public class CrawlerService {

    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    private final List<ServiceConnector> connectors;
    private final IndexerService indexerService;
    private final SyncMetadataService metadataService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${crawler.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Autowired
    public CrawlerService(
            List<ServiceConnector> connectors,
            IndexerService indexerService,
            SyncMetadataService metadataService,
            ObjectMapper objectMapper,
            RestTemplate restTemplate) {
        this.connectors = connectors != null ? connectors : new ArrayList<>();
        this.indexerService = indexerService;
        this.metadataService = metadataService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper; // Use injected ObjectMapper with JSR310 support

        log.info("📊 [INIT] CrawlerService initialisé avec succès : ");
        log.info("   👉 Nombre de connecteurs détectés : {}", this.connectors.size());
        log.info("   👉 IndexerService : {}", indexerService != null ? "✅ Connecté" : "❌ ABSENT");
        log.info("   👉 MetadataService : {}", metadataService != null ? "✅ Connecté" : "❌ ABSENT");
    }

    // ================================================================
    // 1) Écouteur Kafka (Événement Création Utilisateur)
    // ================================================================
    /**
     * Écoute le topic 'user.events' pour détecter la création de nouveaux
     * utilisateurs.
     * Dès qu'un utilisateur est créé, on reçoit l'événement ici et on l'indexe dans
     * Elasticsearch.
     */
    @KafkaListener(topics = "user.events", groupId = "crawler-service-group")
    public void listenUserCreatedEvent(UserCreatedEvent event) {
        try {
            log.info(
                    "� [KAFKA] Événement reçu : Création d'utilisateur détectée (mon explication: un nouveau user s'est inscrit)");
            log.info("   📄 Données reçues : {}", event);

            // Création du document à indexer (Transformation de l'événement en document
            // consultable)
            ServiceDocument doc = ServiceDocument.builder()
                    .id("user_" + event.getId())
                    .serviceType("user")
                    .serviceId(String.valueOf(event.getId()))
                    .title(event.getEmail()) // On utilise l'email comme titre pour la recherche
                    .description("Utilisateur inscrit via Auth Service")
                    .email(event.getEmail())
                    .firstName(event.getFirstName())
                    .lastName(event.getLastName())
                    .indexedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .metadata(objectMapper.convertValue(event, Map.class))
                    .build();

            // Envoi à Elasticsearch via IndexerService
            indexerService.bulkIndex(List.of(doc));
            log.info("✅ [INDEXATION] Utilisateur indexé avec succès dans Elasticsearch. ID: {}", doc.getId());

        } catch (Exception e) {
            log.error("❌ [ERREUR] Échec de l'indexation de l'événement utilisateur : {}", e.getMessage(), e);
        }
    }

    // ================================================================
    // 2) Tâche Planifiée (Scheduled Crawl) pour tous les services
    // ================================================================
    /**
     * Cette méthode s'exécute automatiquement à intervalle régulier (défini dans
     * application.yml).
     * Elle parcourt tous les "Connecteurs" (ex: UserServiceConnector,
     * ListingServiceConnector)
     * pour récupérer leurs données et les mettre à jour dans Elasticsearch.
     */
    @Scheduled(fixedRateString = "${crawler.scheduler.fixed-rate:300000}", initialDelayString = "${crawler.scheduler.initial-delay:60000}")
    @ConditionalOnProperty(name = "crawler.connectors.enabled", havingValue = "true", matchIfMissing = false)
    public void crawlAllServices() {
        crawlAllServices(false);
    }

    public void crawlAllServices(boolean forceResync) {
        if (!schedulerEnabled && !forceResync) { // Allow manual force even if scheduler disabled
            log.info("⚠️ [SCHEDULER] Le planificateur est désactivé, aucun crawl automatique ne sera lancé.");
            return;
        }

        log.info("========================================");
        log.info("🚀 [START] Démarrage du crawl {} de tous les services", forceResync ? "FORCÉ" : "automatique");
        log.info("========================================");

        long startTime = System.currentTimeMillis();
        int totalDocuments = 0;

        for (ServiceConnector connector : connectors) {
            if (!connector.isEnabled()) {
                log.info("ℹ️ [SKIP] Le connecteur pour {} est désactivé.", connector.getServiceType());
                continue;
            }

            try {
                // Appel de la méthode de crawl pour un service spécifique
                int documentsIndexed = crawlService(connector, forceResync);
                totalDocuments += documentsIndexed;
            } catch (Exception e) {
                log.error("❌ [ERREUR] Problème lors du crawl du service {}: {}", connector.getServiceType(),
                        e.getMessage(), e);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("========================================");
        log.info("🏁 [FIN] Crawl global terminé.");
        log.info("   📊 Total documents indexés : {}", totalDocuments);
        log.info("   ⏱️ Durée totale : {} ms", duration);
        log.info("========================================");
    }

    /**
     * Crawl spécifique pour un connecteur donné.
     * Gère la logique de "Sync Incrémentale" vs "Sync Complète".
     */
    public int crawlService(ServiceConnector connector, boolean forceResync) {
        String serviceType = connector.getServiceType();
        log.info("🔍 [SERVICE] Analyse du service : {}", serviceType);

        long startTime = System.currentTimeMillis();

        // Vérifie la dernière fois qu'on a synchronisé ce service
        LocalDateTime lastSync = metadataService.getLastSyncDate(serviceType);

        List<ConnectorResponse> data;
        if (!forceResync && lastSync != null) {
            log.info("   🔄 Mode Incrémental : Récupération des données modifiées depuis {}", lastSync);
            data = connector.fetchIncremental(lastSync);
        } else {
            String reason = forceResync ? "Force Reset demandé" : "Première synchronisation (ou reset)";
            log.info("   🆕 Mode Complet : {}, récupération de TOUTES les données.", reason);
            data = connector.fetchAll();
        }

        log.info("   📦 Données récupérées : {} éléments venant de {}", data.size(), serviceType);

        if (data.isEmpty()) {
            log.info("   💤 Aucune nouvelle donnée à indexer pour {}", serviceType);
            return 0;
        }

        // Transformation des données brutes en documents de recherche unifiés
        List<ServiceDocument> documents = data.stream()
                .map(connector::transform)
                .collect(Collectors.toList());

        // Indexation dans Elasticsearch
        long indexed = indexerService.bulkIndex(documents);

        // Mise à jour de la date de dernière synchro
        metadataService.updateLastSyncDate(serviceType, LocalDateTime.now(), indexed);

        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ [SUCCÈS] Service {} traité : {} documents indexés en {} ms",
                serviceType, indexed, duration);

        return (int) indexed;
    }

    // ================================================================
    // 3) Crawl Manuel (Déclenché par API ou Admin)
    // ================================================================
    public void manualCrawl(String serviceType) {
        log.info("👋 [MANUEL] Crawl manuel demandé pour le service : {}", serviceType);

        ServiceConnector connector = connectors.stream()
                .filter(c -> c.getServiceType().equals(serviceType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "❌ Aucun connecteur trouvé pour le service : " + serviceType));

        crawlService(connector, true);
    }

    // ================================================================
    // 4) Crawl Simple d'une URL (API externe)
    // ================================================================
    public CrawlResult crawl(String apiUrl) {
        log.info("🌐 [API] Tentative de crawl sur l'URL externe : {}", apiUrl);
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> apiResponse = restTemplate.getForObject(apiUrl, Map.class);

            if (apiResponse == null) {
                throw new IllegalArgumentException("L'API a retourné une réponse vide (NULL).");
            }

            // Extraction des documents de la réponse
            List<Map<String, Object>> rawDocuments = new ArrayList<>();
            Object documentsObj = apiResponse.get("documents");

            if (documentsObj instanceof List) {
                rawDocuments = (List<Map<String, Object>>) documentsObj;
                log.info("🔍 [API] Détecté {} documents dans le champ 'documents'", rawDocuments.size());
            } else if (apiResponse.containsKey("id") || apiResponse.containsKey("title")) {
                // Si c'est un document unique directement à la racine
                rawDocuments.add(apiResponse);
                log.info("🔍 [API] Détecté un document unique à la racine");
            }

            // Transformation "best-effort" en ServiceDocuments
            List<ServiceDocument> toIndex = rawDocuments.stream()
                    .map(raw -> {
                        String serviceType = (String) apiResponse.getOrDefault("service", "external");
                        String id = String.valueOf(raw.getOrDefault("id", "ext_" + System.nanoTime()));

                        return ServiceDocument.builder()
                                .id(serviceType + "_" + id)
                                .serviceType(serviceType)
                                .serviceId(id)
                                .title((String) raw.get("title"))
                                .description((String) raw.get("description"))
                                .category((String) raw.get("category"))
                                .location((String) raw.get("location"))
                                .price(raw.get("price") != null ? Double.valueOf(String.valueOf(raw.get("price")))
                                        : null)
                                .indexedAt(LocalDateTime.now())
                                .metadata(raw)
                                .build();
                    })
                    .collect(Collectors.toList());

            // Indexation effective
            long indexedCount = 0;
            if (!toIndex.isEmpty()) {
                indexedCount = indexerService.bulkIndex(toIndex);
                log.info("✅ [API] Indexation réussie : {} documents ajoutés de {}", indexedCount, apiUrl);
            }

            long duration = System.currentTimeMillis() - startTime;

            return CrawlResult.builder()
                    .url(apiUrl)
                    .success(true)
                    .httpStatusCode(200)
                    .extractedData(apiResponse)
                    .itemsFound(toIndex.size())
                    .durationMs(duration)
                    .crawledAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ [API] Échec du crawl sur {} : {}", apiUrl, e.getMessage(), e);

            return CrawlResult.builder()
                    .url(apiUrl)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .durationMs(duration)
                    .crawledAt(LocalDateTime.now())
                    .build();
        }
    }
}
