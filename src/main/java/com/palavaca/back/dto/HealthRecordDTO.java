package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class HealthRecordDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID del animal es obligatorio")
        private UUID animalId;

        private UUID medicationId;

        @NotNull(message = "La fecha del evento es obligatoria")
        private LocalDate eventDate;

        @Size(max = 50)
        private String eventType;

        private String diagnosis;
        private String symptoms;
        private String result;
        private String veterinarianName;
        private BigDecimal dosage;
        private String unit;
        private LocalDate withdrawalEndDate;
        private String notes;
    }

    @Data
    public static class UpdateRequest {
        private UUID medicationId;
        private LocalDate eventDate;
        private String eventType;
        private String diagnosis;
        private String symptoms;
        private String result;
        private String veterinarianName;
        private BigDecimal dosage;
        private String unit;
        private LocalDate withdrawalEndDate;
        private String notes;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID animalId;
        private String animalTagNumber;
        private UUID medicationId;
        private String medicationName;
        private LocalDate eventDate;
        private String eventType;
        private String diagnosis;
        private String symptoms;
        private String result;
        private String veterinarianName;
        private BigDecimal dosage;
        private String unit;
        private LocalDate withdrawalEndDate;
        private String notes;
        private LocalDateTime createdAt;
    }
}