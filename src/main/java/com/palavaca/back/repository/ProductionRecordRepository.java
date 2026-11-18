package com.palavaca.back.repository;

import com.palavaca.back.model.ProductionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductionRecordRepository extends JpaRepository<ProductionRecord, UUID> {
    List<ProductionRecord> findByFarmIdOrderByProductionDateDesc(UUID farmId);

    @Query("""
            select p from ProductionRecord p
            where p.revenue is not null
              and p.revenue > 0
              and not exists (
                  select 1 from FinancialRecord fr
                  where fr.referenceType = :referenceType
                    and fr.referenceId = p.id
                    and fr.deletedAt is null
              )
            """)
    List<ProductionRecord> findWithoutProductionIncome(@Param("referenceType") String referenceType);
}
