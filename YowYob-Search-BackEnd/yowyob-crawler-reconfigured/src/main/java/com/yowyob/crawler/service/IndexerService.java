package com.yowyob.crawler.service;

import com.yowyob.crawler.model.ServiceDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexerService {

    private static final Logger log = LoggerFactory.getLogger(IndexerService.class);

    /**
     * Stubbed: Le Crawler de test n'indexe plus directement dans ES, 
     * il confie cette responsabilité au Monolith.
     */
    public void index(ServiceDocument document) {
        log.debug("IndexerService stub called. Real indexing occurs in Monolith.");
    }

    public long bulkIndex(List<ServiceDocument> documents) {
        if (documents != null) {
            log.info("IndexerService bulkIndex stub called for {} documents.", documents.size());
            return documents.size();
        }
        return 0;
    }

    public void delete(String documentId) {
        log.debug("IndexerService delete stub called.");
    }
}
