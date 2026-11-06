package com.palavaca.back.controller;

import com.palavaca.back.dto.InventoryMovementDTO;
import com.palavaca.back.service.InventoryMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory-movements")
@RequiredArgsConstructor
@Tag(name = "Movimientos de Inventario", description = "Entradas, salidas y ajustes de stock")
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;

    @GetMapping
    @Operation(summary = "Obtener historial de movimientos de un inventario")
    public ResponseEntity<List<InventoryMovementDTO.Response>> getByInventory(@RequestParam("inventory_id") UUID inventoryId) {
        return ResponseEntity.ok(inventoryMovementService.findByInventoryId(inventoryId));
    }

    @PostMapping
    @Operation(summary = "Registrar movimiento de inventario (Entrada/Salida/Ajuste)")
    public ResponseEntity<InventoryMovementDTO.Response> create(@Valid @RequestBody InventoryMovementDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryMovementService.create(request));
    }
}