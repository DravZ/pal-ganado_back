package com.palavaca.back.repository;

import com.palavaca.back.model.SalesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SalesRecordRepository extends JpaRepository<SalesRecord, UUID> {
    List<SalesRecord> findByOrganizationIdOrderBySaleDateDesc(UUID organizationId);
    List<SalesRecord> findByFarmIdOrderBySaleDateDesc(UUID farmId);
    List<SalesRecord> findByOrganizationIdAndSaleDateBetween(UUID organizationId, LocalDate startDate, LocalDate endDate);
}