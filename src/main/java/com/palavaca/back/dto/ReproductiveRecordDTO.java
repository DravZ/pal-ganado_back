package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReproductiveRecordDTO {

    @Data
    public static class CreateRequest {
        private UUID animalId;
        private UUID femaleAnimalId;

        @NotNull(message = "La fecha del evento es obligatoria")
        private LocalDate eventDate;

        @NotNull(message = "El tipo de evento es obligatorio")
        @Size(max = 50)
        private String eventType;

        private UUID sireId;
        private UUID maleAnimalId;
        private LocalDate expectedBirthDate;
        private LocalDate expectedDate;
        private String pregnancyStatus;
        private String notes;
    }

    @Data
    public static class UpdateRequest {
        private LocalDate eventDate;
        private String eventType;
        private UUID sireId;
        private UUID maleAnimalId;
        private LocalDate expectedBirthDate;
        private LocalDate expectedDate;
        private String pregnancyStatus;
        private String notes;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID animalId;
        private UUID femaleAnimalId;
        private String animalTagNumber;
        private LocalDate eventDate;
        private String eventType;
        private UUID sireId;
        private UUID maleAnimalId;
        private String sireTagNumber;
        private LocalDate expectedBirthDate;
        private LocalDate expectedDate;
        private String pregnancyStatus;
        private String notes;
        private LocalDateTime createdAt;
    }
}