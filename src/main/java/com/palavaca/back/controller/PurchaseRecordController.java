package com.palavaca.back.controller;

import com.palavaca.back.dto.PurchaseRecordDTO;
import com.palavaca.back.service.PurchaseRecordService;
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
@RequestMapping({"/api/v1/purchase-records", "/api/v1/purchases"})
@RequiredArgsConstructor
@Tag(name = "Compras", description = "Gestión de compras a proveedores de la organización")
public class PurchaseRecordController {

    private final PurchaseRecordService purchaseRecordService;

    @GetMapping
    @Operation(summary = "Obtener historial de compras de una organización")
    public ResponseEntity<List<PurchaseRecordDTO.Response>> getByOrganization(
            @RequestParam(value = "organization_id", required = false) UUID organizationId,
            @RequestParam(value = "farm_id", required = false) UUID farmId) {
        if (farmId != null) return ResponseEntity.ok(purchaseRecordService.findByFarmId(farmId));
        if (organizationId == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(purchaseRecordService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de compra por ID")
    public ResponseEntity<PurchaseRecordDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(purchaseRecordService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nueva compra")
    public ResponseEntity<PurchaseRecordDTO.Response> create(@Valid @RequestBody PurchaseRecordDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseRecordService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar registro de compra")
    public ResponseEntity<PurchaseRecordDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseRecordDTO.UpdateRequest request) {
        return ResponseEntity.ok(purchaseRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro de compra")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        purchaseRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}