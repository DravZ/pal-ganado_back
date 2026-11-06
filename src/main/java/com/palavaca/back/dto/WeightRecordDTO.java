package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class WeightRecordDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID del animal es obligatorio")
        private UUID animalId;

        @NotNull(message = "La fecha del pesaje es obligatoria")
        private LocalDate weighDate;

        @NotNull(message = "El peso en kilogramos es obligatorio")
        private BigDecimal weightKg;

        private String notes;
    }

    @Data
    public static class UpdateRequest {
        private LocalDate weighDate;
        private BigDecimal weightKg;
        private String notes;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID animalId;
        private String animalTagNumber;
        private LocalDate weighDate;
        private BigDecimal weightKg;
        private String notes;
        private LocalDateTime createdAt;
    }
}