package com.palavaca.back.service;

import com.palavaca.back.dto.InventoryMovementDTO;
import com.palavaca.back.model.Inventory;
import com.palavaca.back.model.InventoryMovement;
import com.palavaca.back.repository.InventoryMovementRepository;
import com.palavaca.back.repository.InventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryMovementDTO.Response> findByInventoryId(UUID inventoryId) {
        return inventoryMovementRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public InventoryMovementDTO.Response create(InventoryMovementDTO.CreateRequest request) {
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + request.getInventoryId()));

        BigDecimal currentQty = inventory.getQuantity() != null ? inventory.getQuantity() : BigDecimal.ZERO;
        BigDecimal moveQty = request.getQuantity();

        if ("IN".equalsIgnoreCase(request.getMovementType())) {
            inventory.setQuantity(currentQty.add(moveQty));
        } else if ("OUT".equalsIgnoreCase(request.getMovementType())) {
            if (currentQty.compareTo(moveQty) < 0) {
                throw new IllegalArgumentException("Stock insuficiente. Disponible: " + currentQty);
            }
            inventory.setQuantity(currentQty.subtract(moveQty));
        } else if ("ADJUSTMENT".equalsIgnoreCase(request.getMovementType())) {
            inventory.setQuantity(moveQty);
        } else {
            throw new IllegalArgumentException("Tipo de movimiento no válido: " + request.getMovementType());
        }

        inventoryRepository.save(inventory);

        InventoryMovement movement = InventoryMovement.builder()
                .inventory(inventory)
                .movementType(request.getMovementType().toUpperCase())
                .quantity(request.getQuantity())
                .reason(request.getReason())
                .notes(request.getNotes())
                .build();

        return mapToResponse(inventoryMovementRepository.save(movement));
    }

    private InventoryMovementDTO.Response mapToResponse(InventoryMovement m) {
        return InventoryMovementDTO.Response.builder()
                .id(m.getId())
                .inventoryId(m.getInventory().getId())
                .movementType(m.getMovementType())
                .quantity(m.getQuantity())
                .reason(m.getReason())
                .notes(m.getNotes())
                .createdAt(m.getCreatedAt())
                .build();
    }
}