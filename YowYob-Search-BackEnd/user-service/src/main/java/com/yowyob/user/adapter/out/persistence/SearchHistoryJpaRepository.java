package com.yowyob.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Interface Spring Data JPA pour les SearchHistory.
 * Connue uniquement de l'adaptateur de persistance.
 */
@Repository
public interface SearchHistoryJpaRepository extends JpaRepository<SearchHistoryJpaEntity, UUID> {

    List<SearchHistoryJpaEntity> findByUserIdOrderBySearchedAtDesc(UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SearchHistoryJpaEntity s WHERE s.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
