package com.palavaca.back.controller;

import com.palavaca.back.dto.UserDTO;
import com.palavaca.back.service.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios por organización")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Obtener usuarios de una organización")
    public ResponseEntity<List<UserDTO.Response>> getByOrganization(@RequestParam("organization_id") UUID organizationId) {
        return ResponseEntity.ok(userService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UserDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<UserDTO.Response> create(@Valid @RequestBody UserDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar datos de un usuario")
    public ResponseEntity<UserDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO.UpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }
}