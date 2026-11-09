package com.palavaca.back.controller;

import com.palavaca.back.dto.SupplierDTO;
import com.palavaca.back.service.HatoAuthorizationService;
import com.palavaca.back.service.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "Gestión de proveedores de la organización")
public class SupplierController {

    private final SupplierService supplierService;
    private final HatoAuthorizationService authorization;

    @GetMapping
    @Operation(summary = "Obtener proveedores de una organización")
    public ResponseEntity<List<SupplierDTO.Response>> getByOrganization(
            Authentication authentication,
            @RequestParam("organization_id") UUID organizationId) {
        authorization.requireOrganization(authentication, organizationId);
        return ResponseEntity.ok(supplierService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por ID")
    public ResponseEntity<SupplierDTO.Response> getById(Authentication authentication, @PathVariable UUID id) {
        SupplierDTO.Response supplier = supplierService.findById(id);
        authorization.requireOrganization(authentication, supplier.getOrganizationId());
        return ResponseEntity.ok(supplier);
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo proveedor")
    public ResponseEntity<SupplierDTO.Response> create(
            Authentication authentication,
            @Valid @RequestBody SupplierDTO.CreateRequest request) {
        authorization.requireOrganization(authentication, request.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un proveedor")
    public ResponseEntity<SupplierDTO.Response> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody SupplierDTO.UpdateRequest request) {
        authorization.requireOrganization(authentication, supplierService.findById(id).getOrganizationId());
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un proveedor")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        authorization.requireOrganization(authentication, supplierService.findById(id).getOrganizationId());
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}