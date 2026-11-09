package com.palavaca.back.controller;

import com.palavaca.back.dto.SalesRecordDTO;
import com.palavaca.back.model.Animal;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.Lot;
import com.palavaca.back.model.SalesRecord;
import com.palavaca.back.model.User;
import com.palavaca.back.service.HatoAuthorizationService;
import com.palavaca.back.service.SalesRecordService;
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
@RequestMapping({"/api/v1/sales-records", "/api/v1/sales"})
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión comercial de ventas de ganado, leche y productos")
public class SalesRecordController {

    private final SalesRecordService salesRecordService;
    private final HatoAuthorizationService authorization;

    @GetMapping
    @Operation(summary = "Obtener historial de ventas de una organización")
    public ResponseEntity<List<SalesRecordDTO.Response>> getByOrganization(
            Authentication authentication,
            @RequestParam(value = "organization_id", required = false) UUID organizationId,
            @RequestParam(value = "farm_id", required = false) UUID farmId) {
        if (farmId != null) {
            authorization.requireFarm(authentication, farmId);
            return ResponseEntity.ok(salesRecordService.findByFarmId(farmId));
        }
        if (organizationId == null) return ResponseEntity.badRequest().build();
        authorization.requireOrganization(authentication, organizationId);
        User user = authorization.currentUser(authentication);
        if (!authorization.isOrganizationAdmin(user)) {
            return ResponseEntity.ok(user.getFarm() == null
                    ? List.of()
                    : salesRecordService.findByFarmId(user.getFarm().getId()));
        }
        return ResponseEntity.ok(salesRecordService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de venta por ID")
    public ResponseEntity<SalesRecordDTO.Response> getById(Authentication authentication, @PathVariable UUID id) {
        authorization.requireSale(authentication, id);
        return ResponseEntity.ok(salesRecordService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nueva venta")
    public ResponseEntity<SalesRecordDTO.Response> create(
            Authentication authentication,
            @Valid @RequestBody SalesRecordDTO.CreateRequest request) {
        if (request.getFarmId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "La finca es obligatoria para registrar la venta.");
        }
        Farm farm = authorization.requireFarm(authentication, request.getFarmId());
        authorization.requireOrganization(authentication, request.getOrganizationId() == null
                ? farm.getOrganization().getId()
                : request.getOrganizationId());
        if (request.getCustomerId() != null) authorization.requireCustomer(authentication, request.getCustomerId());
        if (request.getAnimalId() != null) requireAnimalInFarm(authentication, request.getAnimalId(), farm.getId());
        if (request.getLines() != null) {
            for (SalesRecordDTO.LineRequest line : request.getLines()) {
                if (line.getAnimalId() != null) requireAnimalInFarm(authentication, line.getAnimalId(), farm.getId());
                if (line.getLotId() != null) requireLotInFarm(authentication, line.getLotId(), farm.getId());
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(salesRecordService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar registro de venta")
    public ResponseEntity<SalesRecordDTO.Response> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody SalesRecordDTO.UpdateRequest request) {
        SalesRecord existing = authorization.requireSale(authentication, id);
        UUID farmId = request.getFarmId() == null
                ? existing.getFarm() == null ? null : existing.getFarm().getId()
                : request.getFarmId();
        if (farmId == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
        Farm farm = authorization.requireFarm(authentication, farmId);
        if (request.getCustomerId() != null) authorization.requireCustomer(authentication, request.getCustomerId());
        if (request.getLines() != null) {
            for (SalesRecordDTO.LineRequest line : request.getLines()) {
                if (line.getAnimalId() != null) requireAnimalInFarm(authentication, line.getAnimalId(), farm.getId());
                if (line.getLotId() != null) requireLotInFarm(authentication, line.getLotId(), farm.getId());
            }
        }
        return ResponseEntity.ok(salesRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro de venta")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        authorization.requireSale(authentication, id);
        salesRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void requireAnimalInFarm(Authentication authentication, UUID animalId, UUID farmId) {
        Animal animal = authorization.requireAnimal(authentication, animalId);
        if (animal.getFarm() == null || !farmId.equals(animal.getFarm().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
    }

    private void requireLotInFarm(Authentication authentication, UUID lotId, UUID farmId) {
        Lot lot = authorization.requireLot(authentication, lotId);
        if (!farmId.equals(lot.getFarm().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
    }
}