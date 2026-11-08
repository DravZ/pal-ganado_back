package com.palavaca.back.controller;

import com.palavaca.back.dto.SalesRecordDTO;
import com.palavaca.back.service.SalesRecordService;
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
@RequestMapping({"/api/v1/sales-records", "/api/v1/sales"})
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión comercial de ventas de ganado, leche y productos")
public class SalesRecordController {

    private final SalesRecordService salesRecordService;

    @GetMapping
    @Operation(summary = "Obtener historial de ventas de una organización")
    public ResponseEntity<List<SalesRecordDTO.Response>> getByOrganization(
            @RequestParam(value = "organization_id", required = false) UUID organizationId,
            @RequestParam(value = "farm_id", required = false) UUID farmId) {
        if (farmId != null) return ResponseEntity.ok(salesRecordService.findByFarmId(farmId));
        if (organizationId == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(salesRecordService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de venta por ID")
    public ResponseEntity<SalesRecordDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(salesRecordService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nueva venta")
    public ResponseEntity<SalesRecordDTO.Response> create(@Valid @RequestBody SalesRecordDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesRecordService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar registro de venta")
    public ResponseEntity<SalesRecordDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody SalesRecordDTO.UpdateRequest request) {
        return ResponseEntity.ok(salesRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro de venta")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        salesRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}