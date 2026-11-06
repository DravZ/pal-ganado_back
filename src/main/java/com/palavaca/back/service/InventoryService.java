package com.palavaca.back.service;

import com.palavaca.back.dto.InventoryDTO;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.Inventory;
import com.palavaca.back.model.Medication;
import com.palavaca.back.model.Product;
import com.palavaca.back.repository.FarmRepository;
import com.palavaca.back.repository.InventoryRepository;
import com.palavaca.back.repository.MedicationRepository;
import com.palavaca.back.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final FarmRepository farmRepository;
    private final ProductRepository productRepository;
    private final MedicationRepository medicationRepository;

    @Transactional(readOnly = true)
    public List<InventoryDTO.Response> findByFarmId(UUID farmId) {
        return inventoryRepository.findByFarmId(farmId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryDTO.Response findById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));
        return mapToResponse(inventory);
    }

    @Transactional
    public InventoryDTO.Response create(InventoryDTO.CreateRequest request) {
        if (request.getProductId() == null && request.getMedicationId() == null) {
            throw new IllegalArgumentException("Debe asociar un Producto o un Medicamento al inventario");
        }

        Farm farm = farmRepository.findById(request.getFarmId())
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada con ID: " + request.getFarmId()));

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + request.getProductId()));
        }

        Medication medication = null;
        if (request.getMedicationId() != null) {
            medication = medicationRepository.findById(request.getMedicationId())
                    .orElseThrow(() -> new EntityNotFoundException("Medicamento no encontrado con ID: " + request.getMedicationId()));
        }

        Inventory inventory = Inventory.builder()
                .farm(farm)
                .product(product)
                .medication(medication)
                .quantity(request.getQuantity())
                .minStock(request.getMinStock())
                .build();

        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryDTO.Response update(UUID id, InventoryDTO.UpdateRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con ID: " + id));

        if (request.getQuantity() != null) inventory.setQuantity(request.getQuantity());
        if (request.getMinStock() != null) inventory.setMinStock(request.getMinStock());

        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public void delete(UUID id) {
        if (!inventoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Inventario no encontrado con ID: " + id);
        }
        inventoryRepository.deleteById(id);
    }

    private InventoryDTO.Response mapToResponse(Inventory i) {
        return InventoryDTO.Response.builder()
                .id(i.getId())
                .farmId(i.getFarm().getId())
                .farmName(i.getFarm().getName())
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productName(i.getProduct() != null ? i.getProduct().getName() : null)
                .medicationId(i.getMedication() != null ? i.getMedication().getId() : null)
                .medicationName(i.getMedication() != null ? i.getMedication().getName() : null)
                .quantity(i.getQuantity())
                .minStock(i.getMinStock())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }
}