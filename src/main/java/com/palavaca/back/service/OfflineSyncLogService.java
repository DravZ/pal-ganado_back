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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfflineSyncLogService {

    private final OfflineSyncLogRepository offlineSyncLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<OfflineSyncLogDTO.Response> findPendingByUser(UUID userId) {
        return offlineSyncLogRepository.findByUserIdAndSyncStatus(userId, "PENDING").stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public OfflineSyncLogDTO.Response create(OfflineSyncLogDTO.CreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + request.getUserId()));

        OfflineSyncLog log = OfflineSyncLog.builder()
                .user(user)
                .entityName(request.getEntityName())
                .actionType(request.getActionType().toUpperCase())
                .payload(request.getPayload())
                .syncStatus("PENDING")
                .build();

        return mapToResponse(offlineSyncLogRepository.save(log));
    }

    @Transactional
    public OfflineSyncLogDTO.Response markAsProcessed(UUID id) {
        OfflineSyncLog log = offlineSyncLogRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Log de sincronización no encontrado"));

        log.setSyncStatus("PROCESSED");
        log.setSyncedAt(LocalDateTime.now());

        return mapToResponse(offlineSyncLogRepository.save(log));
    }

    private OfflineSyncLogDTO.Response mapToResponse(OfflineSyncLog log) {
        return OfflineSyncLogDTO.Response.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
                .userName(log.getUser().getName())
                .entityName(log.getEntityName())
                .actionType(log.getActionType())
                .payload(log.getPayload())
                .syncStatus(log.getSyncStatus())
                .errorMessage(log.getErrorMessage())
                .createdAt(log.getCreatedAt())
                .syncedAt(log.getSyncedAt())
                .build();
    }
}