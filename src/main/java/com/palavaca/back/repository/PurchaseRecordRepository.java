package com.palavaca.back.repository;

import com.palavaca.back.model.PurchaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, UUID> {
    List<PurchaseRecord> findByOrganizationIdOrderByPurchaseDateDesc(UUID organizationId);
    List<PurchaseRecord> findByOrganizationIdAndPurchaseDateBetween(UUID organizationId, LocalDate startDate, LocalDate endDate);
}