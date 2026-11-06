package com.palavaca.back.controller;

import com.palavaca.back.dto.MedicationDTO;
import com.palavaca.back.service.MedicationService;
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
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
@Tag(name = "Medicamentos", description = "Catálogo general de medicamentos y vacunas")
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping
    @Operation(summary = "Obtener catálogo de medicamentos")
    public ResponseEntity<List<MedicationDTO.Response>> getAll() {
        return ResponseEntity.ok(medicationService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener medicamento por UUID")
    public ResponseEntity<MedicationDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(medicationService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo medicamento en el catálogo")
    public ResponseEntity<MedicationDTO.Response> create(@Valid @RequestBody MedicationDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicationService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar información de un medicamento")
    public ResponseEntity<MedicationDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody MedicationDTO.UpdateRequest request) {
        return ResponseEntity.ok(medicationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un medicamento")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        medicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}