package com.palavaca.back.controller;

import com.palavaca.back.dto.OfflineSyncLogDTO;
import com.palavaca.back.service.OfflineSyncLogService;
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
@RequestMapping("/api/v1/offline-sync")
@RequiredArgsConstructor
@Tag(name = "Sincronización Offline", description = "Cola de sincronización de datos de campo")
public class OfflineSyncLogController {

    private final OfflineSyncLogService offlineSyncLogService;

    @GetMapping
    @Operation(summary = "Obtener registros de sincronización pendientes por usuario")
    public ResponseEntity<List<OfflineSyncLogDTO.Response>> getPending(@RequestParam("user_id") UUID userId) {
        return ResponseEntity.ok(offlineSyncLogService.findPendingByUser(userId));
    }

    @PostMapping
    @Operation(summary = "Registrar evento capturado en modo offline")
    public ResponseEntity<OfflineSyncLogDTO.Response> create(@Valid @RequestBody OfflineSyncLogDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offlineSyncLogService.create(request));
    }

    @PatchMapping("/{id}/processed")
    @Operation(summary = "Marcar evento como procesado correctamente")
    public ResponseEntity<OfflineSyncLogDTO.Response> markAsProcessed(@PathVariable UUID id) {
        return ResponseEntity.ok(offlineSyncLogService.markAsProcessed(id));
    }
}