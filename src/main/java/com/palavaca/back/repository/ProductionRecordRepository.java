package com.palavaca.back.repository;

import com.palavaca.back.model.ProductionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductionRecordRepository extends JpaRepository<ProductionRecord, UUID> {
    List<ProductionRecord> findByFarmIdOrderByProductionDateDesc(UUID farmId);
}
