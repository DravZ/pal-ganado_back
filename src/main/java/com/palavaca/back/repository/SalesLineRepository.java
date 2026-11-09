package com.palavaca.back.repository;

import com.palavaca.back.model.SalesLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalesLineRepository extends JpaRepository<SalesLine, UUID> {
    List<SalesLine> findBySaleId(UUID saleId);
}
