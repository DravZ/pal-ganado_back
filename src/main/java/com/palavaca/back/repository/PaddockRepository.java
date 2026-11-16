package com.palavaca.back.repository;

import com.palavaca.back.model.Paddock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaddockRepository extends JpaRepository<Paddock, UUID> {

    @Query("select p from Paddock p where p.farm.id = :farmId and p.deletedAt is null")
    List<Paddock> findByFarmId(@Param("farmId") UUID farmId);
}
