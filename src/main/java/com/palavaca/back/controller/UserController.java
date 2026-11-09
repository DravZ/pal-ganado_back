package com.palavaca.back.controller;

import com.palavaca.back.dto.UserDTO;
import com.palavaca.back.service.HatoAuthorizationService;
import com.palavaca.back.service.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios por organización")
public class UserController {

    private final UserService userService;
    private final HatoAuthorizationService authorizationService;

    @GetMapping
    @Operation(summary = "Obtener usuarios de una organización")
    public ResponseEntity<List<UserDTO.Response>> getByOrganization(
            @RequestParam("organization_id") UUID organizationId,
            Authentication authentication) {
        authorizationService.requireOrganizationAdmin(authentication, organizationId);
        return ResponseEntity.ok(userService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UserDTO.Response> getById(@PathVariable UUID id, Authentication authentication) {
        UserDTO.Response user = userService.findById(id);
        authorizationService.requireOrganizationAdmin(authentication, user.getOrganizationId());
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<UserDTO.Response> create(
            @Valid @RequestBody UserDTO.CreateRequest request,
            Authentication authentication) {
        authorizationService.requireOrganizationAdmin(authentication, request.getOrganizationId());
        if (request.getFarmId() != null) authorizationService.requireFarm(authentication, request.getFarmId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar datos de un usuario")
    public ResponseEntity<UserDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO.UpdateRequest request,
            Authentication authentication) {
        UserDTO.Response existing = userService.findById(id);
        authorizationService.requireOrganizationAdmin(authentication, existing.getOrganizationId());
        if (request.getFarmId() != null) authorizationService.requireFarm(authentication, request.getFarmId());
        return ResponseEntity.ok(userService.update(id, request));
    }
}