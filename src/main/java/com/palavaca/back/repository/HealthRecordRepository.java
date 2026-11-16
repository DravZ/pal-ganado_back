package com.palavaca.back.repository;

import com.palavaca.back.model.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, UUID> {

    @Query("select h from HealthRecord h where h.animal.id = :animalId and h.deletedAt is null order by h.eventDate desc")
    List<HealthRecord> findByAnimalIdOrderByEventDateDesc(@Param("animalId") UUID animalId);
}
