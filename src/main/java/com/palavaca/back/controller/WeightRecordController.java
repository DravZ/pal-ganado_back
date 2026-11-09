package com.palavaca.back.controller;

import com.palavaca.back.dto.WeightRecordDTO;
import com.palavaca.back.service.HatoAuthorizationService;
import com.palavaca.back.service.WeightRecordService;
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
@RequestMapping({"/api/v1/weight-records", "/api/v1/animal-weights"})
@RequiredArgsConstructor
@Tag(name = "Control de Pesos", description = "Historial de pesaje y desarrollo corporal")
public class WeightRecordController {

    private final WeightRecordService weightRecordService;
    private final HatoAuthorizationService authorization;

    @GetMapping
    @Operation(summary = "Obtener historial de peso de un animal")
    public ResponseEntity<List<WeightRecordDTO.Response>> getByAnimal(
            Authentication authentication,
            @RequestParam("animal_id") UUID animalId) {
        authorization.requireAnimal(authentication, animalId);
        return ResponseEntity.ok(weightRecordService.findByAnimalId(animalId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de peso por ID")
    public ResponseEntity<WeightRecordDTO.Response> getById(Authentication authentication, @PathVariable UUID id) {
        authorization.requireWeightRecord(authentication, id);
        return ResponseEntity.ok(weightRecordService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo peso de un animal")
    public ResponseEntity<WeightRecordDTO.Response> create(
            Authentication authentication,
            @Valid @RequestBody WeightRecordDTO.CreateRequest request) {
        authorization.requireAnimal(authentication, request.getAnimalId());
        return ResponseEntity.status(HttpStatus.CREATED).body(weightRecordService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar registro de peso")
    public ResponseEntity<WeightRecordDTO.Response> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody WeightRecordDTO.UpdateRequest request) {
        authorization.requireWeightRecord(authentication, id);
        return ResponseEntity.ok(weightRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro de peso")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        authorization.requireWeightRecord(authentication, id);
        weightRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}