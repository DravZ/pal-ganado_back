package com.palavaca.back.repository;

import com.palavaca.back.model.MovementAnimal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovementAnimalRepository extends JpaRepository<MovementAnimal, UUID> {
    List<MovementAnimal> findByMovementId(UUID movementId);
}
