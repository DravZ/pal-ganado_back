package com.palavaca.back.repository;

import com.palavaca.back.model.ReproductiveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReproductiveRecordRepository extends JpaRepository<ReproductiveRecord, UUID> {
    List<ReproductiveRecord> findByAnimalIdOrderByEventDateDesc(UUID animalId);
}