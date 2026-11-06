package com.palavaca.back.controller;

import com.palavaca.back.dto.ReproductiveRecordDTO;
import com.palavaca.back.service.ReproductiveRecordService;
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
@RequestMapping("/api/v1/reproductive-records")
@RequiredArgsConstructor
@Tag(name = "Registros Reproductivos", description = "Control de celos, inseminación, palpación y partos")
public class ReproductiveRecordController {

    private final ReproductiveRecordService reproductiveRecordService;

    @GetMapping
    @Operation(summary = "Obtener historial reproductivo de una vaca")
    public ResponseEntity<List<ReproductiveRecordDTO.Response>> getByAnimal(@RequestParam("animal_id") UUID animalId) {
        return ResponseEntity.ok(reproductiveRecordService.findByAnimalId(animalId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro reproductivo por ID")
    public ResponseEntity<ReproductiveRecordDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reproductiveRecordService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo evento reproductivo")
    public ResponseEntity<ReproductiveRecordDTO.Response> create(@Valid @RequestBody ReproductiveRecordDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reproductiveRecordService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar evento reproductivo")
    public ResponseEntity<ReproductiveRecordDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReproductiveRecordDTO.UpdateRequest request) {
        return ResponseEntity.ok(reproductiveRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar registro reproductivo")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reproductiveRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}