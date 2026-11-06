package com.palavaca.back.controller;

import com.palavaca.back.dto.MilkProductionDTO;
import com.palavaca.back.service.MilkProductionService;
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
@RequestMapping("/api/v1/milk-productions")
@RequiredArgsConstructor
@Tag(name = "Producción de Leche", description = "Control individual de pesaje de leche por animal")
public class MilkProductionController {

    private final MilkProductionService milkProductionService;

    @GetMapping
    @Operation(summary = "Obtener historial de leche de un animal")
    public ResponseEntity<List<MilkProductionDTO.Response>> getByAnimal(@RequestParam("animal_id") UUID animalId) {
        return ResponseEntity.ok(milkProductionService.findByAnimalId(animalId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de producción por ID")
    public ResponseEntity<MilkProductionDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(milkProductionService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo ordeño")
    public ResponseEntity<MilkProductionDTO.Response> create(@Valid @RequestBody MilkProductionDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(milkProductionService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar registro de ordeño")
    public ResponseEntity<MilkProductionDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody MilkProductionDTO.UpdateRequest request) {
        return ResponseEntity.ok(milkProductionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar registro de ordeño")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        milkProductionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}