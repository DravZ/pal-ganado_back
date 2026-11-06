package com.palavaca.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class OfflineSyncLogDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID del usuario es obligatorio")
        private UUID userId;

        @NotBlank(message = "El nombre de la entidad es obligatorio")
        @Size(max = 100)
        private String entityName;

        @NotBlank(message = "El tipo de acción es obligatorio")
        @Size(max = 30)
        private String actionType;

        private String payload;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID userId;
        private String userName;
        private String entityName;
        private String actionType;
        private String payload;
        private String syncStatus;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime syncedAt;
    }
}