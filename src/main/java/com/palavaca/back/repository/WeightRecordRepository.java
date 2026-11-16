package com.palavaca.back.repository;

import com.palavaca.back.model.WeightRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WeightRecordRepository extends JpaRepository<WeightRecord, UUID> {

    @Query("select w from WeightRecord w where w.animal.id = :animalId and w.deletedAt is null order by w.weighDate desc")
    List<WeightRecord> findByAnimalIdOrderByWeighDateDesc(@Param("animalId") UUID animalId);
}
