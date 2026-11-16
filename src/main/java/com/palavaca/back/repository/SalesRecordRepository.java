package com.palavaca.back.repository;

import com.palavaca.back.model.SalesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SalesRecordRepository extends JpaRepository<SalesRecord, UUID> {

    @Query("select s from SalesRecord s where s.organization.id = :organizationId and s.deletedAt is null order by s.saleDate desc")
    List<SalesRecord> findByOrganizationIdOrderBySaleDateDesc(@Param("organizationId") UUID organizationId);

    @Query("select s from SalesRecord s where s.farm.id = :farmId and s.deletedAt is null order by s.saleDate desc")
    List<SalesRecord> findByFarmIdOrderBySaleDateDesc(@Param("farmId") UUID farmId);

    @Query("select s from SalesRecord s where s.organization.id = :organizationId and s.saleDate between :startDate and :endDate and s.deletedAt is null")
    List<SalesRecord> findByOrganizationIdAndSaleDateBetween(@Param("organizationId") UUID organizationId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
