package com.palavaca.back.repository;

import com.palavaca.back.model.ReproductiveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReproductiveRecordRepository extends JpaRepository<ReproductiveRecord, UUID> {

    @Query("select r from ReproductiveRecord r where r.animal.id = :animalId and r.deletedAt is null order by r.eventDate desc")
    List<ReproductiveRecord> findByAnimalIdOrderByEventDateDesc(@Param("animalId") UUID animalId);
}
