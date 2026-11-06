package com.palavaca.back.controller;

import com.palavaca.back.dto.PaddockDTO;
import com.palavaca.back.service.PaddockService;
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
@RequestMapping("/api/v1/paddocks")
@RequiredArgsConstructor
@Tag(name = "Potreros", description = "Gestión de potreros o divisiones por finca")
public class PaddockController {

    private final PaddockService paddockService;

    @GetMapping
    @Operation(summary = "Obtener potreros de una finca")
    public ResponseEntity<List<PaddockDTO.Response>> getByFarm(@RequestParam("farm_id") UUID farmId) {
        return ResponseEntity.ok(paddockService.findByFarmId(farmId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener potrero por ID")
    public ResponseEntity<PaddockDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(paddockService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo potrero")
    public ResponseEntity<PaddockDTO.Response> create(@Valid @RequestBody PaddockDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paddockService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un potrero")
    public ResponseEntity<PaddockDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody PaddockDTO.UpdateRequest request) {
        return ResponseEntity.ok(paddockService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un potrero")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        paddockService.delete(id);
        return ResponseEntity.noContent().build();
    }
}