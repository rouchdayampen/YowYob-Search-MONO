package com.yowyob.crawler.service;

import com.yowyob.crawler.model.SyncMetadata;
import com.yowyob.crawler.Repository.SyncMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SyncMetadataService {

    private final SyncMetadataRepository repository;

    public SyncMetadataService(SyncMetadataRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public LocalDateTime getLastSyncDate(String serviceType) {
        return repository.findByServiceType(serviceType)
                .map(SyncMetadata::getLastSyncDate)
                .orElse(null);
    }

    @Transactional
    public void updateLastSyncDate(String serviceType, LocalDateTime date, long documentsIndexed) {
        SyncMetadata metadata = repository.findByServiceType(serviceType)
                .orElseGet(() -> SyncMetadata.builder()
                        .serviceType(serviceType)
                        .documentsIndexed(0L)
                        .build());

        metadata.setLastSyncDate(date);
        metadata.setDocumentsIndexed(documentsIndexed);

        repository.save(metadata);
    }
}
