package com.palavaca.back.repository;

import com.palavaca.back.model.Paddock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaddockRepository extends JpaRepository<Paddock, UUID> {

    @Query("select p from Paddock p where p.farm.id = :farmId and p.deletedAt is null")
    List<Paddock> findByFarmId(@Param("farmId") UUID farmId);

    /**
     * Bloquea el potrero para serializar las validaciones de capacidad. Dos altas o traslados
     * simultáneos sobre el mismo potrero no pueden exceder la capacidad máxima.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Paddock p where p.id = :id and p.deletedAt is null")
    Optional<Paddock> findByIdForUpdate(@Param("id") UUID id);
}
