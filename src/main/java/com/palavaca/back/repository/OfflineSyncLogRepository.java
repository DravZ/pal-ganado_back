package com.palavaca.back.repository;

import com.palavaca.back.model.OfflineSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfflineSyncLogRepository extends JpaRepository<OfflineSyncLog, UUID> {
    List<OfflineSyncLog> findByUserIdAndSyncStatus(UUID userId, String syncStatus);
}