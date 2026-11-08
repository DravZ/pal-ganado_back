package com.palavaca.back.service;

import com.palavaca.back.dto.OfflineSyncLogDTO;
import com.palavaca.back.model.OfflineSyncLog;
import com.palavaca.back.model.User;
import com.palavaca.back.repository.OfflineSyncLogRepository;
import com.palavaca.back.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfflineSyncLogService {

    private final OfflineSyncLogRepository offlineSyncLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<OfflineSyncLogDTO.Response> findPendingByUser(UUID userId) {
        return offlineSyncLogRepository.findByUserIdAndSyncStatus(userId, "PENDING").stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfflineSyncLogDTO.Response> findPending() {
        return offlineSyncLogRepository.findBySyncStatusOrderByCreatedAtDesc("PENDING").stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public OfflineSyncLogDTO.Response create(OfflineSyncLogDTO.CreateRequest request) {
        User user = request.getUserId() == null ? null : userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + request.getUserId()));
        String operation = request.getOperationType() != null ? request.getOperationType() : request.getActionType();
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("El tipo de operación es obligatorio");
        }

        OfflineSyncLog log = OfflineSyncLog.builder()
                .user(user)
                .entityName(request.getEntityName())
                .entityId(request.getEntityId())
                .actionType(operation.toUpperCase())
                .payload(serializePayload(request.getPayload()))
                .syncStatus("PENDING")
                .build();

        return mapToResponse(offlineSyncLogRepository.save(log));
    }

    @Transactional
    public OfflineSyncLogDTO.Response markAsProcessed(UUID id) {
        OfflineSyncLog log = offlineSyncLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Log de sincronización no encontrado"));

        log.setSyncStatus("SYNCED");
        log.setSyncedAt(LocalDateTime.now());

        return mapToResponse(offlineSyncLogRepository.save(log));
    }

    private OfflineSyncLogDTO.Response mapToResponse(OfflineSyncLog log) {
        return OfflineSyncLogDTO.Response.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(log.getUser() != null ? log.getUser().getName() : null)
                .entityId(log.getEntityId())
                .entityName(log.getEntityName())
                .actionType(log.getActionType())
                .operationType(log.getActionType())
                .payload(deserializePayload(log.getPayload()))
                .syncStatus(log.getSyncStatus())
                .status(log.getSyncStatus())
                .retryCount(log.getRetryCount())
                .errorMessage(log.getErrorMessage())
                .createdAt(log.getCreatedAt())
                .syncedAt(log.getSyncedAt())
                .processedAt(log.getSyncedAt())
                .build();
    }

    private String serializePayload(JsonNode payload) {
        if (payload == null) return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("El payload de sincronización no es JSON válido", e);
        }
    }

    private JsonNode deserializePayload(String payload) {
        if (payload == null) return null;
        try {
            return objectMapper.readTree(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException("El payload almacenado no contiene JSON válido", e);
        }
    }
}