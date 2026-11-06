package com.palavaca.back.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLogDTO {

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID userId;
        private String userName;
        private String action;
        private String targetEntity;
        private UUID targetId;
        private String details;
        private LocalDateTime createdAt;
    }
}