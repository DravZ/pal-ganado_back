package com.palavaca.back.repository;

import com.palavaca.back.model.PurchaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, UUID> {

    @Query("select p from PurchaseRecord p where p.organization.id = :organizationId and p.deletedAt is null order by p.purchaseDate desc")
    List<PurchaseRecord> findByOrganizationIdOrderByPurchaseDateDesc(@Param("organizationId") UUID organizationId);

    @Query("select p from PurchaseRecord p where p.farm.id = :farmId and p.deletedAt is null order by p.purchaseDate desc")
    List<PurchaseRecord> findByFarmIdOrderByPurchaseDateDesc(@Param("farmId") UUID farmId);

    @Query("select p from PurchaseRecord p where p.organization.id = :organizationId and p.purchaseDate between :startDate and :endDate and p.deletedAt is null")
    List<PurchaseRecord> findByOrganizationIdAndPurchaseDateBetween(@Param("organizationId") UUID organizationId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
