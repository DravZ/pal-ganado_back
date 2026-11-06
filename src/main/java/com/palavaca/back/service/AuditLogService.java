package com.palavaca.back.service;

import com.palavaca.back.dto.AuditLogDTO;
import com.palavaca.back.model.AuditLog;
import com.palavaca.back.model.User;
import com.palavaca.back.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLogDTO.Response> findByUserId(UUID userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void logAction(User user, String action, String targetEntity, UUID targetId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .targetEntity(targetEntity)
                .targetId(targetId)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }

    private AuditLogDTO.Response mapToResponse(AuditLog log) {
        return AuditLogDTO.Response.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(log.getUser() != null ? log.getUser().getName() : "SISTEMA")
                .action(log.getAction())
                .targetEntity(log.getTargetEntity())
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}