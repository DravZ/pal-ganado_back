package com.palavaca.back.repository;

import com.palavaca.back.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {
    List<Medication> findByCategoryContainingIgnoreCase(String category);
    List<Medication> findByNameContainingIgnoreCase(String name);
}