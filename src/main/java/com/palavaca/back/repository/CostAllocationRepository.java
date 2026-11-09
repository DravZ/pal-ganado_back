package com.palavaca.back.repository;

import com.palavaca.back.model.CostAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CostAllocationRepository extends JpaRepository<CostAllocation, UUID> {
    List<CostAllocation> findByFarmIdOrderByAllocationDateDesc(UUID farmId);
}
