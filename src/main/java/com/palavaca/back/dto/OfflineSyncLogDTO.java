package com.palavaca.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

public class OfflineSyncLogDTO {

    @Data
    public static class CreateRequest {
        private UUID userId;

        @NotBlank(message = "El nombre de la entidad es obligatorio")
        @Size(max = 100)
        private String entityName;

        @Size(max = 30)
        private String actionType;
        private String operationType;

        private UUID entityId;
        private JsonNode payload;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID userId;
        private String userName;
        private UUID entityId;
        private String entityName;
        private String actionType;
        private String operationType;
        private JsonNode payload;
        private String syncStatus;
        private String status;
        private Integer retryCount;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime syncedAt;
        private LocalDateTime processedAt;
    }
}