package com.palavaca.back.repository;

import com.palavaca.back.model.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, UUID> {
    List<FinancialRecord> findByOrganizationIdOrderByRecordDateDesc(UUID organizationId);
    List<FinancialRecord> findByOrganizationIdAndRecordDateBetween(UUID organizationId, LocalDate startDate, LocalDate endDate);
}