package com.palavaca.back.controller;

import com.palavaca.back.dto.FarmDTO;
import com.palavaca.back.model.User;
import com.palavaca.back.service.HatoAuthorizationService;
import com.palavaca.back.service.FarmService;
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
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
@Tag(name = "Fincas", description = "Gestión de ranchos o fincas de la organización")
public class FarmController {

    private final FarmService farmService;
    private final HatoAuthorizationService authorizationService;

    @GetMapping
    @Operation(summary = "Obtener fincas de una organización")
    public ResponseEntity<List<FarmDTO.Response>> getByOrganization(
            @RequestParam("organization_id") UUID organizationId,
            Authentication authentication) {
        authorizationService.requireOrganization(authentication, organizationId);
        User user = authorizationService.currentUser(authentication);
        List<FarmDTO.Response> farms = farmService.findByOrganizationId(organizationId);
        if (!authorizationService.isOrganizationAdmin(user)) {
            UUID allowedFarmId = user.getFarm() == null ? null : user.getFarm().getId();
            farms = farms.stream().filter(farm -> farm.getId().equals(allowedFarmId)).toList();
        }
        return ResponseEntity.ok(farms);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener finca por ID")
    public ResponseEntity<FarmDTO.Response> getById(@PathVariable UUID id, Authentication authentication) {
        FarmDTO.Response farm = farmService.findById(id);
        authorizationService.requireOrganization(authentication, farm.getOrganizationId());
        User user = authorizationService.currentUser(authentication);
        if (!authorizationService.isOrganizationAdmin(user)) authorizationService.requireFarm(authentication, id);
        return ResponseEntity.ok(farm);
    }

    @PostMapping
    @Operation(summary = "Registrar nueva finca")
    public ResponseEntity<FarmDTO.Response> create(
            @Valid @RequestBody FarmDTO.CreateRequest request,
            Authentication authentication) {
        authorizationService.requireOrganizationAdmin(authentication, request.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(farmService.create(request));
    }

    @RequestMapping(path = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Operation(summary = "Actualizar datos de una finca")
    public ResponseEntity<FarmDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody FarmDTO.UpdateRequest request,
            Authentication authentication) {
        FarmDTO.Response existing = farmService.findById(id);
        authorizationService.requireOrganizationAdmin(authentication, existing.getOrganizationId());
        return ResponseEntity.ok(farmService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una finca")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        FarmDTO.Response existing = farmService.findById(id);
        authorizationService.requireOrganizationAdmin(authentication, existing.getOrganizationId());
        farmService.delete(id);
        return ResponseEntity.noContent().build();
    }
}