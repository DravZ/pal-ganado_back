package com.palavaca.back.repository;

import com.palavaca.back.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    List<Inventory> findByFarmId(UUID farmId);
    Optional<Inventory> findByFarmIdAndProductId(UUID farmId, UUID productId);
    Optional<Inventory> findByFarmIdAndMedicationId(UUID farmId, UUID medicationId);
}