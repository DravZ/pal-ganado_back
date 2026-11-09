package com.palavaca.back.repository;

import com.palavaca.back.model.FeedingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedingRecordRepository extends JpaRepository<FeedingRecord, UUID> {
    List<FeedingRecord> findByFarmIdOrderByFeedingDateDesc(UUID farmId);
}
