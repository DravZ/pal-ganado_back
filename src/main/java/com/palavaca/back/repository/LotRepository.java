package com.palavaca.back.repository;

import com.palavaca.back.model.Lot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LotRepository extends JpaRepository<Lot, UUID> {
    List<Lot> findByFarmId(UUID farmId);
}
