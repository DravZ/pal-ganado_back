package com.palavaca.back.repository;

import com.palavaca.back.model.SyncChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SyncChangeRepository extends JpaRepository<SyncChange, Long> {

    @Query("""
            select change from SyncChange change
            where change.id > :cursor
              and change.id <= :upperBound
              and (change.organizationId = :organizationId
                   or (change.organizationId is null and change.resourceType = 'medications'))
              and (change.farmId is null or change.farmId in :farmIds)
              and (:organizationAdmin = true or change.resourceType <> 'users' or change.recordId = :userId)
            order by change.id
            """)
    List<SyncChange> findPageForScope(@Param("cursor") long cursor,
                                      @Param("upperBound") long upperBound,
                                      @Param("organizationId") UUID organizationId,
                                      @Param("farmIds") List<UUID> farmIds,
                                      @Param("organizationAdmin") boolean organizationAdmin,
                                      @Param("userId") UUID userId,
                                      org.springframework.data.domain.Pageable pageable);

    @Query("""
            select change from SyncChange change
            where change.id > :cursor
              and change.id <= :upperBound
              and (change.organizationId = :organizationId
                   or (change.organizationId is null and change.resourceType = 'medications'))
              and change.farmId is null
              and (:organizationAdmin = true or change.resourceType <> 'users' or change.recordId = :userId)
            order by change.id
            """)
    List<SyncChange> findPageWithoutFarmScope(@Param("cursor") long cursor,
                                              @Param("upperBound") long upperBound,
                                              @Param("organizationId") UUID organizationId,
                                              @Param("organizationAdmin") boolean organizationAdmin,
                                              @Param("userId") UUID userId,
                                              org.springframework.data.domain.Pageable pageable);

    @Query("select coalesce(max(change.id), 0) from SyncChange change")
    long currentCursor();
}
