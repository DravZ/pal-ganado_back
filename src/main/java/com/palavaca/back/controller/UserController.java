package com.palavaca.back.controller;

import com.palavaca.back.dto.UserDTO;
import com.palavaca.back.service.HatoAuthorizationService;
import com.palavaca.back.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
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
    private final SecurityContextRepository securityContextRepository;

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
        com.palavaca.back.model.User current = authorizationService.currentUser(authentication);
        if (!current.getId().equals(id)) {
            authorizationService.requireOrganizationAdmin(authentication, user.getOrganizationId());
        }
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
            Authentication authentication,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        UserDTO.Response existing = userService.findById(id);
        com.palavaca.back.model.User current = authorizationService.currentUser(authentication);
        boolean selfUpdate = current.getId().equals(id);
        if (!selfUpdate) {
            authorizationService.requireOrganizationAdmin(authentication, existing.getOrganizationId());
        } else if (request.getRole() != null || request.getStatus() != null || request.getFarmId() != null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Solo un administrador puede modificar rol, estado o finca.");
        }
        if (request.getFarmId() != null) authorizationService.requireFarm(authentication, request.getFarmId());
        UserDTO.Response updated = userService.update(id, request);
        if (selfUpdate && !current.getEmail().equals(updated.getEmail())) {
            Authentication updatedAuthentication = new UsernamePasswordAuthenticationToken(
                    updated.getEmail(),
                    authentication.getCredentials(),
                    authentication.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(updatedAuthentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, servletRequest, servletResponse);
        }
        return ResponseEntity.ok(updated);
    }
}