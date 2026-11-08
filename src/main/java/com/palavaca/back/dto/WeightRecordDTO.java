package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonAlias;
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

        private LocalDate weighDate;

        @NotNull(message = "El peso en kilogramos es obligatorio")
        @JsonAlias("weight")
        private BigDecimal weightKg;

        private LocalDateTime measuredAt;
        private LocalDateTime date;
        private String method;
        private String notes;
    }

    @Data
    public static class UpdateRequest {
        private LocalDate weighDate;
        @JsonAlias("weight")
        private BigDecimal weightKg;
        private LocalDateTime measuredAt;
        private String method;
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
        private BigDecimal weight;
        private LocalDateTime measuredAt;
        private String method;
        private String notes;
        private LocalDateTime createdAt;
    }
}