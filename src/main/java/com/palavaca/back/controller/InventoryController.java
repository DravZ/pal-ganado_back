package com.palavaca.back.controller;

import com.palavaca.back.dto.InventoryDTO;
import com.palavaca.back.service.InventoryService;
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
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "Control de existencias de insumos y medicamentos por finca")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "Obtener inventario de una finca")
    public ResponseEntity<List<InventoryDTO.Response>> getByFarm(@RequestParam("farm_id") UUID farmId) {
        return ResponseEntity.ok(inventoryService.findByFarmId(farmId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener inventario por ID")
    public ResponseEntity<InventoryDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo registro de inventario")
    public ResponseEntity<InventoryDTO.Response> create(@Valid @RequestBody InventoryDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nivel de inventario o stock mínimo")
    public ResponseEntity<InventoryDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryDTO.UpdateRequest request) {
        return ResponseEntity.ok(inventoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar registro de inventario")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}