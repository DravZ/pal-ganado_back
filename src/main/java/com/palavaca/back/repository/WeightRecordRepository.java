package com.palavaca.back.repository;

import com.palavaca.back.model.WeightRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WeightRecordRepository extends JpaRepository<WeightRecord, UUID> {
    List<WeightRecord> findByAnimalIdOrderByWeighDateDesc(UUID animalId);
}