package com.palavaca.back.controller;

import com.palavaca.back.dto.FarmDTO;
import com.palavaca.back.service.FarmService;
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
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
@Tag(name = "Fincas", description = "Gestión de ranchos o fincas de la organización")
public class FarmController {

    private final FarmService farmService;

    @GetMapping
    @Operation(summary = "Obtener fincas de una organización")
    public ResponseEntity<List<FarmDTO.Response>> getByOrganization(@RequestParam("organization_id") UUID organizationId) {
        return ResponseEntity.ok(farmService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener finca por ID")
    public ResponseEntity<FarmDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(farmService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nueva finca")
    public ResponseEntity<FarmDTO.Response> create(@Valid @RequestBody FarmDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de una finca")
    public ResponseEntity<FarmDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody FarmDTO.UpdateRequest request) {
        return ResponseEntity.ok(farmService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una finca")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        farmService.delete(id);
        return ResponseEntity.noContent().build();
    }
}