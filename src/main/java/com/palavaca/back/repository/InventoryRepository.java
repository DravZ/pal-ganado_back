package com.palavaca.back.repository;

import com.palavaca.back.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    @Query("select i from Inventory i where i.farm.id = :farmId and i.deletedAt is null")
    List<Inventory> findByFarmId(@Param("farmId") UUID farmId);

    Optional<Inventory> findByFarmIdAndProductId(UUID farmId, UUID productId);
    Optional<Inventory> findByFarmIdAndMedicationId(UUID farmId, UUID medicationId);
}
