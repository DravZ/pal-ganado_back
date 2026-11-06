package com.palavaca.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class MedicationDTO {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        private String name;

        private String activeIngredient;
        private String category;
        private String presentation;
        private String defaultUnit;
        private Integer withdrawalDaysMilk;
        private Integer withdrawalDaysMeat;
        private BigDecimal defaultDosage;
    }

    @Data
    public static class UpdateRequest {
        @Size(max = 150)
        private String name;
        private String activeIngredient;
        private String category;
        private String presentation;
        private String defaultUnit;
        private Integer withdrawalDaysMilk;
        private Integer withdrawalDaysMeat;
        private BigDecimal defaultDosage;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private String name;
        private String activeIngredient;
        private String category;
        private String presentation;
        private String defaultUnit;
        private Integer withdrawalDaysMilk;
        private Integer withdrawalDaysMeat;
        private BigDecimal defaultDosage;
        private LocalDateTime createdAt;
    }
}