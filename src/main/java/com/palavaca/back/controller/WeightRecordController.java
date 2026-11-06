package com.palavaca.back.controller;

import com.palavaca.back.dto.WeightRecordDTO;
import com.palavaca.back.service.WeightRecordService;
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
@RequestMapping("/api/v1/weight-records")
@RequiredArgsConstructor
@Tag(name = "Control de Pesos", description = "Historial de pesaje y desarrollo corporal")
public class WeightRecordController {

    private final WeightRecordService weightRecordService;

    @GetMapping
    @Operation(summary = "Obtener historial de peso de un animal")
    public ResponseEntity<List<WeightRecordDTO.Response>> getByAnimal(@RequestParam("animal_id") UUID animalId) {
        return ResponseEntity.ok(weightRecordService.findByAnimalId(animalId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de peso por ID")
    public ResponseEntity<WeightRecordDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(weightRecordService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo peso de un animal")
    public ResponseEntity<WeightRecordDTO.Response> create(@Valid @RequestBody WeightRecordDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(weightRecordService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar registro de peso")
    public ResponseEntity<WeightRecordDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody WeightRecordDTO.UpdateRequest request) {
        return ResponseEntity.ok(weightRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro de peso")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        weightRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}