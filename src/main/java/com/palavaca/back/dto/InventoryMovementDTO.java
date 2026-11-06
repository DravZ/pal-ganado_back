package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class InventoryMovementDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de inventario es obligatorio")
        private UUID inventoryId;

        @NotNull(message = "El tipo de movimiento es obligatorio (IN, OUT, ADJUSTMENT)")
        @Size(max = 30)
        private String movementType;

        @NotNull(message = "La cantidad es obligatoria")
        private BigDecimal quantity;

        private String reason;
        private String notes;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID inventoryId;
        private String movementType;
        private BigDecimal quantity;
        private String reason;
        private String notes;
        private LocalDateTime createdAt;
    }
}