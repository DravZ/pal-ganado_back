package com.palavaca.back.controller;

import com.palavaca.back.dto.AuditLogDTO;
import com.palavaca.back.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Trazabilidad de acciones de usuarios")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Obtener historial de auditoría de un usuario")
    public ResponseEntity<List<AuditLogDTO.Response>> getByUserId(@RequestParam("user_id") UUID userId) {
        return ResponseEntity.ok(auditLogService.findByUserId(userId));
    }
}