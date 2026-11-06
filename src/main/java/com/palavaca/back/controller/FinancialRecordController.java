package com.palavaca.back.controller;

import com.palavaca.back.dto.FinancialRecordDTO;
import com.palavaca.back.service.FinancialRecordService;
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
@RequestMapping("/api/v1/financial-records")
@RequiredArgsConstructor
@Tag(name = "Finanzas", description = "Control de ingresos, egresos y flujo de caja")
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;

    @GetMapping
    @Operation(summary = "Obtener historial financiero de una organización")
    public ResponseEntity<List<FinancialRecordDTO.Response>> getByOrganization(@RequestParam("organization_id") UUID organizationId) {
        return ResponseEntity.ok(financialRecordService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento financiero por ID")
    public ResponseEntity<FinancialRecordDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(financialRecordService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo movimiento financiero (Ingreso/Egreso)")
    public ResponseEntity<FinancialRecordDTO.Response> create(@Valid @RequestBody FinancialRecordDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financialRecordService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar movimiento financiero")
    public ResponseEntity<FinancialRecordDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody FinancialRecordDTO.UpdateRequest request) {
        return ResponseEntity.ok(financialRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar movimiento financiero")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        financialRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}