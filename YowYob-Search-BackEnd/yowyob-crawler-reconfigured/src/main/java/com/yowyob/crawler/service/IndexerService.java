package com.yowyob.crawler.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.yowyob.crawler.model.ServiceDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class IndexerService {

    private static final Logger log = LoggerFactory.getLogger(IndexerService.class);

    @Value("${elasticsearch.index.name}")
    private String indexName;

    private final ElasticsearchClient esClient;

    @Autowired(required = false)
    public IndexerService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    /**
     * Indexe un document unique
     */
    public void index(ServiceDocument document) {
        try {
            IndexResponse response = esClient.index(i -> i
                    .index(indexName)
                    .id(document.getId())        // <-- ID corrigé
                    .document(document)
            );

            log.debug("Document indexed: {} with result: {}",
                    response.id(), response.result());

        } catch (IOException e) {
            log.error("Error indexing document: {}", document.getId(), e);
            throw new RuntimeException("Failed to index document", e);
        }
    }

    /**
     * Indexe plusieurs documents en masse
     */
    public long bulkIndex(List<ServiceDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("No documents to index");
            return 0;
        }

        try {
            log.info("Starting bulk indexing of {} documents", documents.size());

            BulkRequest.Builder br = new BulkRequest.Builder();

            for (ServiceDocument doc : documents) {
                br.operations(op -> op
                        .index(idx -> idx
                                .index(indexName)
                                .id(doc.getId())    // <-- ID corrigé
                                .document(doc)
                        )
                );
            }

            BulkResponse result = esClient.bulk(br.build());

            if (result.errors()) {
                log.error("Bulk indexing had errors");
                for (BulkResponseItem item : result.items()) {
                    if (item.error() != null) {
                        log.error("Error indexing document {}: {}",
                                item.id(), item.error().reason());
                    }
                }
            } else {
                log.info("Successfully indexed {} documents", documents.size());
            }

            // Retourner le nombre de documents indexés avec succès
            long successCount = result.items().stream()
                    .filter(item -> item.error() == null)
                    .count();

            return successCount;

        } catch (IOException e) {
            log.error("Error during bulk indexing", e);
            throw new RuntimeException("Failed to bulk index documents", e);
        }
    }

    /**
     * Supprime un document
     */
    public void delete(String documentId) {
        try {
            esClient.delete(d -> d
                    .index(indexName)
                    .id(documentId)
            );
            log.info("Document deleted: {}", documentId);
        } catch (IOException e) {
            log.error("Error deleting document: {}", documentId, e);
        }
    }
}
