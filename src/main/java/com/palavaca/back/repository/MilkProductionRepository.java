package com.palavaca.back.repository;

import com.palavaca.back.model.MilkProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MilkProductionRepository extends JpaRepository<MilkProduction, UUID> {
    List<MilkProduction> findByAnimalIdOrderByRecordDateDesc(UUID animalId);
    List<MilkProduction> findByAnimalIdAndRecordDateBetween(UUID animalId, LocalDate startDate, LocalDate endDate);
}