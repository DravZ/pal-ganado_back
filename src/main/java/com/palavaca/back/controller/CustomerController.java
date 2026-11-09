package com.palavaca.back.controller;

import com.palavaca.back.dto.CustomerDTO;
import com.palavaca.back.service.HatoAuthorizationService;
import com.palavaca.back.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes de la organización")
public class CustomerController {

    private final CustomerService customerService;
    private final HatoAuthorizationService authorization;

    @GetMapping
    @Operation(summary = "Obtener clientes de una organización")
    public ResponseEntity<List<CustomerDTO.Response>> getByOrganization(
            Authentication authentication,
            @RequestParam("organization_id") UUID organizationId) {
        authorization.requireOrganization(authentication, organizationId);
        return ResponseEntity.ok(customerService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<CustomerDTO.Response> getById(Authentication authentication, @PathVariable UUID id) {
        authorization.requireCustomer(authentication, id);
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo cliente")
    public ResponseEntity<CustomerDTO.Response> create(
            Authentication authentication,
            @Valid @RequestBody CustomerDTO.CreateRequest request) {
        authorization.requireOrganization(authentication, request.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un cliente")
    public ResponseEntity<CustomerDTO.Response> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody CustomerDTO.UpdateRequest request) {
        authorization.requireCustomer(authentication, id);
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un cliente")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        authorization.requireCustomer(authentication, id);
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}