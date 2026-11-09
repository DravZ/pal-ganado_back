package com.palavaca.back.repository;

import com.palavaca.back.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovementRepository extends JpaRepository<Movement, UUID> {
    List<Movement> findByFarmIdOrderByMovementDateDesc(UUID farmId);
}
