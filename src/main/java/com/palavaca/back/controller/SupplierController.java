package com.palavaca.back.controller;

import com.palavaca.back.dto.SupplierDTO;
import com.palavaca.back.service.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "Gestión de proveedores de la organización")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "Obtener proveedores de una organización")
    public ResponseEntity<List<SupplierDTO.Response>> getByOrganization(@RequestParam("organization_id") UUID organizationId) {
        return ResponseEntity.ok(supplierService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por ID")
    public ResponseEntity<SupplierDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo proveedor")
    public ResponseEntity<SupplierDTO.Response> create(@Valid @RequestBody SupplierDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un proveedor")
    public ResponseEntity<SupplierDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody SupplierDTO.UpdateRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un proveedor")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}