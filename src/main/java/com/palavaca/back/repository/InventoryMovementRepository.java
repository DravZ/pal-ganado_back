package com.palavaca.back.repository;

import com.palavaca.back.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {
    List<InventoryMovement> findByInventoryIdOrderByCreatedAtDesc(UUID inventoryId);
}