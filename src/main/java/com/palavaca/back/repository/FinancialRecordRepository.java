package com.palavaca.back.repository;

import com.palavaca.back.model.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, UUID> {

    @Query("select f from FinancialRecord f where f.organization.id = :organizationId and f.deletedAt is null order by f.recordDate desc")
    List<FinancialRecord> findByOrganizationIdOrderByRecordDateDesc(@Param("organizationId") UUID organizationId);

    @Query("select f from FinancialRecord f where f.organization.id = :organizationId and f.recordDate between :startDate and :endDate and f.deletedAt is null")
    List<FinancialRecord> findByOrganizationIdAndRecordDateBetween(@Param("organizationId") UUID organizationId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
