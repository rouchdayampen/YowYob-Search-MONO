package com.yowyob.crawler.extractor;

import org.jsoup.nodes.Document;

import java.util.Map;

/**
 * Interface pour les extracteurs de sites spécifiques
 */
public interface SiteExtractor {
    
    /**
     * Vérifie si cet extracteur peut gérer l'URL donnée
     */
    boolean canHandle(String url);
    
    /**
     * Priorité de l'extracteur (plus petit = plus prioritaire)
     * GenericExtractor devrait avoir la priorité la plus basse
     */
    int getPriority();
    
    /**
     * Extrait les données du document HTML
     */
    Map<String, Object> extract(Document document, String url);
    
    /**
     * Nom de l'extracteur pour les logs
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
