package com.yowyob.crawler.Connector;

import com.yowyob.crawler.model.ConnectorResponse;
import com.yowyob.crawler.model.ServiceDocument;

import java.time.LocalDateTime;
import java.util.List;

public interface ServiceConnector {

    /**
     * Recupere toutes les donnees du service
     */
    List<ConnectorResponse> fetchAll();

    /**
     * Recupere les donnees modifiees depuis lastSync
     */
    List<ConnectorResponse> fetchIncremental(LocalDateTime lastSync);

    /**
     * Transforme les donnees au format ElasticSearch
     */
    ServiceDocument transform(ConnectorResponse response);

    /**
     * Retourne le type de service
     */
    String getServiceType();

    /**
     * Verifie si le connecteur est actif
     */
    boolean isEnabled();
}