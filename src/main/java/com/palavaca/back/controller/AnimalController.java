package com.palavaca.back.controller;

import com.palavaca.back.dto.AnimalDTO;
import com.palavaca.back.model.Animal;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.Lot;
import com.palavaca.back.model.Paddock;
import com.palavaca.back.model.User;
import com.palavaca.back.service.AnimalService;
import com.palavaca.back.service.HatoAuthorizationService;
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
@RequestMapping("/api/v1/animals")
@RequiredArgsConstructor
@Tag(name = "Animales", description = "Gestión individual del hato ganadero")
public class AnimalController {

    private final AnimalService animalService;
    private final HatoAuthorizationService authorization;

    @GetMapping
    @Operation(summary = "Obtener ganado de una organización o finca")
    public ResponseEntity<List<AnimalDTO.Response>> getAnimals(
            Authentication authentication,
            @RequestParam("organization_id") UUID organizationId,
            @RequestParam(value = "farm_id", required = false) UUID farmId,
            @RequestParam(value = "include_photo", defaultValue = "false") boolean includePhoto) {
        authorization.requireOrganization(authentication, organizationId);
        if (farmId != null) {
            authorization.requireFarm(authentication, farmId);
            return ResponseEntity.ok(animalService.findByFarmId(farmId, includePhoto));
        }
        User user = authorization.currentUser(authentication);
        if (!authorization.isOrganizationAdmin(user)) {
            return ResponseEntity.ok(user.getFarm() == null
                    ? List.of()
                    : animalService.findByFarmId(user.getFarm().getId(), includePhoto));
        }
        return ResponseEntity.ok(animalService.findByOrganizationId(organizationId, includePhoto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener expediente de un animal por ID")
    public ResponseEntity<AnimalDTO.Response> getById(Authentication authentication, @PathVariable UUID id) {
        authorization.requireAnimal(authentication, id);
        return ResponseEntity.ok(animalService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo animal en el hato")
    public ResponseEntity<AnimalDTO.Response> create(
            Authentication authentication,
            @Valid @RequestBody AnimalDTO.CreateRequest request) {
        authorization.requireOrganization(authentication, request.getOrganizationId());
        User user = authorization.currentUser(authentication);
        if (request.getFarmId() == null && !authorization.isOrganizationAdmin(user) && user.getFarm() != null) {
            request.setFarmId(user.getFarm().getId());
        }
        Farm farm = request.getFarmId() == null ? null : authorization.requireFarm(authentication, request.getFarmId());
        if (farm == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Selecciona una finca autorizada.");
        }
        if (request.getLotId() != null) {
            Lot lot = authorization.requireLot(authentication, request.getLotId());
            if (!farm.getId().equals(lot.getFarm().getId())) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
            }
        }
        if (request.getPaddockId() != null) {
            Paddock paddock = authorization.requirePaddock(authentication, request.getPaddockId());
            if (!farm.getId().equals(paddock.getFarm().getId())) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
            }
        }
        if (request.getSireId() != null) requireAnimalInFarm(authentication, request.getSireId(), farm.getId());
        if (request.getDamId() != null) requireAnimalInFarm(authentication, request.getDamId(), farm.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un animal")
    public ResponseEntity<AnimalDTO.Response> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody AnimalDTO.UpdateRequest request) {
        Animal existing = authorization.requireAnimal(authentication, id);
        UUID farmId = request.getFarmId() != null
                ? request.getFarmId()
                : existing.getFarm() == null ? null : existing.getFarm().getId();
        if (farmId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Selecciona una finca autorizada.");
        }
        Farm farm = authorization.requireFarm(authentication, farmId);
        if (request.getPaddockId() != null
                && !farm.getId().equals(authorization.requirePaddock(authentication, request.getPaddockId()).getFarm().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
        if (request.getLotId() != null
                && !farm.getId().equals(authorization.requireLot(authentication, request.getLotId()).getFarm().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
        if (request.getSireId() != null) requireAnimalInFarm(authentication, request.getSireId(), farm.getId());
        if (request.getDamId() != null) requireAnimalInFarm(authentication, request.getDamId(), farm.getId());
        return ResponseEntity.ok(animalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja/Eliminar un animal")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        authorization.requireAnimal(authentication, id);
        animalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void requireAnimalInFarm(Authentication authentication, UUID animalId, UUID farmId) {
        Animal animal = authorization.requireAnimal(authentication, animalId);
        if (animal.getFarm() == null || !farmId.equals(animal.getFarm().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
    }
}