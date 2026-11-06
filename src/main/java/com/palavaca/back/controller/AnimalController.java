package com.palavaca.back.controller;

import com.palavaca.back.dto.AnimalDTO;
import com.palavaca.back.service.AnimalService;
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
@RequestMapping("/api/v1/animals")
@RequiredArgsConstructor
@Tag(name = "Animales", description = "Gestión individual del hato ganadero")
public class AnimalController {

    private final AnimalService animalService;

    @GetMapping
    @Operation(summary = "Obtener ganado de una organización o finca")
    public ResponseEntity<List<AnimalDTO.Response>> getAnimals(
            @RequestParam("organization_id") UUID organizationId,
            @RequestParam(value = "farm_id", required = false) UUID farmId) {
        if (farmId != null) {
            return ResponseEntity.ok(animalService.findByFarmId(farmId));
        }
        return ResponseEntity.ok(animalService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener expediente de un animal por ID")
    public ResponseEntity<AnimalDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(animalService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo animal en el hato")
    public ResponseEntity<AnimalDTO.Response> create(@Valid @RequestBody AnimalDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un animal")
    public ResponseEntity<AnimalDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody AnimalDTO.UpdateRequest request) {
        return ResponseEntity.ok(animalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja/Eliminar un animal")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        animalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}