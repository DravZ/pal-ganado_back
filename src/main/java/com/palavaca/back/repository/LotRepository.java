package com.palavaca.back.repository;

import com.palavaca.back.model.Lot;
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
public interface LotRepository extends JpaRepository<Lot, UUID> {
    List<Lot> findByFarmId(UUID farmId);

    /** Bloquea el lote para editar su ubicación o sus datos sin carreras entre traslados. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lot l where l.id = :id")
    Optional<Lot> findByIdForUpdate(@Param("id") UUID id);

    long countByCurrentPaddockId(UUID paddockId);
}
