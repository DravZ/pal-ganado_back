package com.palavaca.back.controller;

import com.palavaca.back.dto.OrganizationDTO;
import com.palavaca.back.service.OrganizationService;
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
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizaciones", description = "Endpoints para la gestión de organizaciones principales")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    @Operation(summary = "Obtener todas las organizaciones")
    public ResponseEntity<List<OrganizationDTO.Response>> getAll() {
        return ResponseEntity.ok(organizationService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una organización por UUID")
    public ResponseEntity<OrganizationDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva organización")
    public ResponseEntity<OrganizationDTO.Response> create(@Valid @RequestBody OrganizationDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de una organización")
    public ResponseEntity<OrganizationDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationDTO.UpdateRequest request) {
        return ResponseEntity.ok(organizationService.update(id, request));
    }
}