package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class InventoryDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de la finca es obligatorio")
        private UUID farmId;

        private UUID productId;
        private UUID medicationId;

        @NotNull(message = "La cantidad inicial es obligatoria")
        private BigDecimal quantity;

        private BigDecimal minStock;
    }

    @Data
    public static class UpdateRequest {
        private BigDecimal quantity;
        private BigDecimal minStock;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID farmId;
        private String farmName;
        private UUID productId;
        private String productName;
        private UUID medicationId;
        private String medicationName;
        private BigDecimal quantity;
        private BigDecimal minStock;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}