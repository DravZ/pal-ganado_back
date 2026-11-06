package com.palavaca.back.repository;

import com.palavaca.back.model.Paddock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaddockRepository extends JpaRepository<Paddock, UUID> {
    List<Paddock> findByFarmId(UUID farmId);
}