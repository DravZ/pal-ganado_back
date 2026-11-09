package com.palavaca.back.controller;

import com.palavaca.back.dto.OrganizationDTO;
import com.palavaca.back.model.User;
import com.palavaca.back.service.HatoAuthorizationService;
import com.palavaca.back.service.OrganizationService;
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
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizaciones", description = "Endpoints para la gestión de organizaciones principales")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final HatoAuthorizationService authorizationService;

    @GetMapping
    @Operation(summary = "Obtener todas las organizaciones")
    public ResponseEntity<List<OrganizationDTO.Response>> getAll(Authentication authentication) {
        User user = authorizationService.currentUser(authentication);
        return ResponseEntity.ok(List.of(organizationService.findById(user.getOrganization().getId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una organización por UUID")
    public ResponseEntity<OrganizationDTO.Response> getById(
            @PathVariable UUID id,
            Authentication authentication) {
        authorizationService.requireOrganization(authentication, id);
        return ResponseEntity.ok(organizationService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva organización")
    public ResponseEntity<OrganizationDTO.Response> create(@Valid @RequestBody OrganizationDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationService.create(request));
    }

    @RequestMapping(path = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Operation(summary = "Actualizar datos de una organización")
    public ResponseEntity<OrganizationDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationDTO.UpdateRequest request,
            Authentication authentication) {
        authorizationService.requireOrganizationAdmin(authentication, id);
        return ResponseEntity.ok(organizationService.update(id, request));
    }
}