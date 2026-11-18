package com.palavaca.back.repository;

import com.palavaca.back.model.FeedingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FeedingRecordRepository extends JpaRepository<FeedingRecord, UUID> {
    List<FeedingRecord> findByFarmIdOrderByFeedingDateDesc(UUID farmId);

    @Query("""
            select f from FeedingRecord f
            where f.cost is not null
              and f.cost > 0
              and not exists (
                  select 1 from Expense e
                  where e.referenceType = :referenceType
                    and e.referenceId = f.id
                    and e.deletedAt is null
              )
            """)
    List<FeedingRecord> findWithoutFeedingExpense(@Param("referenceType") String referenceType);
}
