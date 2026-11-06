package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MilkProductionDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID del animal es obligatorio")
        private UUID animalId;

        @NotNull(message = "La fecha del ordeño es obligatoria")
        private LocalDate recordDate;

        private BigDecimal morningLiters;
        private BigDecimal afternoonLiters;

        @NotNull(message = "El total de litros es obligatorio")
        private BigDecimal totalLiters;

        private String notes;
    }

    @Data
    public static class UpdateRequest {
        private LocalDate recordDate;
        private BigDecimal morningLiters;
        private BigDecimal afternoonLiters;
        private BigDecimal totalLiters;
        private String notes;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID animalId;
        private String animalTagNumber;
        private LocalDate recordDate;
        private BigDecimal morningLiters;
        private BigDecimal afternoonLiters;
        private BigDecimal totalLiters;
        private String notes;
        private LocalDateTime createdAt;
    }
}