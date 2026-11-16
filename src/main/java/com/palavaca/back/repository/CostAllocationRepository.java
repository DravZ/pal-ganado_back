package com.palavaca.back.repository;

import com.palavaca.back.model.CostAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CostAllocationRepository extends JpaRepository<CostAllocation, UUID> {

    @Query("select c from CostAllocation c where c.farm.id = :farmId and c.deletedAt is null order by c.allocationDate desc")
    List<CostAllocation> findByFarmIdOrderByAllocationDateDesc(@Param("farmId") UUID farmId);
}
