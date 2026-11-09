package com.palavaca.back.repository;

import com.palavaca.back.model.PurchaseLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseLineRepository extends JpaRepository<PurchaseLine, UUID> {
    List<PurchaseLine> findByPurchaseId(UUID purchaseId);
}
