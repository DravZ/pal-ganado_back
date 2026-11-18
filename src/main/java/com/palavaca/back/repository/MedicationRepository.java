package com.palavaca.back.repository;

import com.palavaca.back.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {

    List<Medication> findByDeletedAtIsNull();

    @Query("select m from Medication m where lower(m.category) like lower(concat('%', :category, '%')) and m.deletedAt is null")
    List<Medication> findByCategoryContainingIgnoreCase(@Param("category") String category);

    @Query("select m from Medication m where lower(m.name) like lower(concat('%', :name, '%')) and m.deletedAt is null")
    List<Medication> findByNameContainingIgnoreCase(@Param("name") String name);
}
