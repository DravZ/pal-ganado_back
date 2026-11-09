package com.palavaca.back.controller;

import com.palavaca.back.dto.HealthRecordDTO;
import com.palavaca.back.service.HatoAuthorizationService;
import com.palavaca.back.service.HealthRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/health-records", "/api/v1/health-events"})
@RequiredArgsConstructor
@Tag(name = "Registros Sanitarios", description = "Gestión de salud, vacunas y tratamientos por animal")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final HatoAuthorizationService authorization;

    @GetMapping
    @Operation(summary = "Obtener historial de salud de un animal")
    public ResponseEntity<List<HealthRecordDTO.Response>> getByAnimal(
            Authentication authentication,
            @RequestParam("animal_id") UUID animalId) {
        authorization.requireAnimal(authentication, animalId);
        return ResponseEntity.ok(healthRecordService.findByAnimalId(animalId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro sanitario por ID")
    public ResponseEntity<HealthRecordDTO.Response> getById(Authentication authentication, @PathVariable UUID id) {
        HealthRecordDTO.Response record = healthRecordService.findById(id);
        authorization.requireAnimal(authentication, record.getAnimalId());
        return ResponseEntity.ok(record);
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo evento de salud/tratamiento")
    public ResponseEntity<HealthRecordDTO.Response> create(
            Authentication authentication,
            @Valid @RequestBody HealthRecordDTO.CreateRequest request) {
        authorization.requireAnimal(authentication, request.getAnimalId());
        return ResponseEntity.status(HttpStatus.CREATED).body(healthRecordService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar registro sanitario")
    public ResponseEntity<HealthRecordDTO.Response> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody HealthRecordDTO.UpdateRequest request) {
        HealthRecordDTO.Response existing = healthRecordService.findById(id);
        authorization.requireAnimal(authentication, existing.getAnimalId());
        return ResponseEntity.ok(healthRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar registro sanitario")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        HealthRecordDTO.Response existing = healthRecordService.findById(id);
        authorization.requireAnimal(authentication, existing.getAnimalId());
        healthRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}