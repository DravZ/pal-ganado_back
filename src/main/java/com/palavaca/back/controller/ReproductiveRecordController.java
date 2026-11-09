package com.palavaca.back.controller;

import com.palavaca.back.dto.ReproductiveRecordDTO;
import com.palavaca.back.model.Animal;
import com.palavaca.back.model.ReproductiveRecord;
import com.palavaca.back.service.HatoAuthorizationService;
import com.palavaca.back.service.ReproductiveRecordService;
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
@RequestMapping({"/api/v1/reproductive-records", "/api/v1/reproductive-events"})
@RequiredArgsConstructor
@Tag(name = "Registros Reproductivos", description = "Control de celos, inseminación, palpación y partos")
public class ReproductiveRecordController {

    private final ReproductiveRecordService reproductiveRecordService;
    private final HatoAuthorizationService authorization;

    @GetMapping
    @Operation(summary = "Obtener historial reproductivo de una vaca")
    public ResponseEntity<List<ReproductiveRecordDTO.Response>> getByAnimal(
            Authentication authentication,
            @RequestParam("animal_id") UUID animalId) {
        authorization.requireAnimal(authentication, animalId);
        return ResponseEntity.ok(reproductiveRecordService.findByAnimalId(animalId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro reproductivo por ID")
    public ResponseEntity<ReproductiveRecordDTO.Response> getById(Authentication authentication, @PathVariable UUID id) {
        authorization.requireReproductiveRecord(authentication, id);
        return ResponseEntity.ok(reproductiveRecordService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo evento reproductivo")
    public ResponseEntity<ReproductiveRecordDTO.Response> create(
            Authentication authentication,
            @Valid @RequestBody ReproductiveRecordDTO.CreateRequest request) {
        UUID femaleId = request.getAnimalId() == null ? request.getFemaleAnimalId() : request.getAnimalId();
        Animal female = authorization.requireAnimal(authentication, femaleId);
        UUID maleId = request.getSireId() == null ? request.getMaleAnimalId() : request.getSireId();
        if (maleId != null) {
            Animal male = authorization.requireAnimal(authentication, maleId);
            if (female.getFarm() == null || male.getFarm() == null
                    || !female.getFarm().getId().equals(male.getFarm().getId())) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN);
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(reproductiveRecordService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar evento reproductivo")
    public ResponseEntity<ReproductiveRecordDTO.Response> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody ReproductiveRecordDTO.UpdateRequest request) {
        ReproductiveRecord existing = authorization.requireReproductiveRecord(authentication, id);
        UUID maleId = request.getSireId() == null ? request.getMaleAnimalId() : request.getSireId();
        if (maleId != null) {
            Animal male = authorization.requireAnimal(authentication, maleId);
            if (existing.getAnimal().getFarm() == null || male.getFarm() == null
                    || !existing.getAnimal().getFarm().getId().equals(male.getFarm().getId())) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN);
            }
        }
        return ResponseEntity.ok(reproductiveRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar registro reproductivo")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        authorization.requireReproductiveRecord(authentication, id);
        reproductiveRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}