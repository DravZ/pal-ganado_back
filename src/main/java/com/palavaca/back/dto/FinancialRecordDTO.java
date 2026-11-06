package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class FinancialRecordDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de la organización es obligatorio")
        private UUID organizationId;

        @NotNull(message = "La fecha del movimiento es obligatoria")
        private LocalDate recordDate;

        @NotNull(message = "El tipo de transacción es obligatorio (INCOME/EXPENSE)")
        @Size(max = 20)
        private String transactionType;

        private String category;

        @NotNull(message = "El monto es obligatorio")
        private BigDecimal amount;

        private String description;
    }

    @Data
    public static class UpdateRequest {
        private LocalDate recordDate;
        private String transactionType;
        private String category;
        private BigDecimal amount;
        private String description;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID organizationId;
        private LocalDate recordDate;
        private String transactionType;
        private String category;
        private BigDecimal amount;
        private String description;
        private LocalDateTime createdAt;
    }
}