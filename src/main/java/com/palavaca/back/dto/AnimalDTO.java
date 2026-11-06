package com.palavaca.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class AnimalDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de la organización es obligatorio")
        private UUID organizationId;

        private UUID farmId;
        private UUID paddockId;

        @NotBlank(message = "El número de arete es obligatorio")
        @Size(max = 50)
        private String tagNumber;

        private String name;

        @NotBlank(message = "El género es obligatorio")
        @Size(max = 10)
        private String gender;

        private LocalDate birthDate;
        private String breed;
        private String category;
        private String purpose;
        private BigDecimal weightAtBirth;
        private BigDecimal currentWeight;

        private UUID sireId;
        private UUID damId;
    }

    @Data
    public static class UpdateRequest {
        private UUID farmId;
        private UUID paddockId;
        private String tagNumber;
        private String name;
        private String gender;
        private LocalDate birthDate;
        private String breed;
        private String category;
        private String purpose;
        private BigDecimal currentWeight;
        private String status;
        private UUID sireId;
        private UUID damId;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID organizationId;
        private UUID farmId;
        private String farmName;
        private UUID paddockId;
        private String paddockName;
        private String tagNumber;
        private String name;
        private String gender;
        private LocalDate birthDate;
        private String breed;
        private String category;
        private String purpose;
        private BigDecimal weightAtBirth;
        private BigDecimal currentWeight;
        private String status;
        private UUID sireId;
        private String sireTagNumber;
        private UUID damId;
        private String damTagNumber;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}