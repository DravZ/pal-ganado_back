package com.palavaca.back.repository;

import com.palavaca.back.model.MilkProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MilkProductionRepository extends JpaRepository<MilkProduction, UUID> {

    @Query("select m from MilkProduction m where m.animal.id = :animalId and m.deletedAt is null order by m.recordDate desc")
    List<MilkProduction> findByAnimalIdOrderByRecordDateDesc(@Param("animalId") UUID animalId);

    @Query("select m from MilkProduction m where m.animal.id = :animalId and m.recordDate between :startDate and :endDate and m.deletedAt is null")
    List<MilkProduction> findByAnimalIdAndRecordDateBetween(@Param("animalId") UUID animalId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
