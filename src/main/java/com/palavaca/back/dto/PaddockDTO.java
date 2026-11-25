package com.palavaca.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaddockDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de la finca es obligatorio")
        private UUID farmId;

        @NotBlank(message = "El nombre del potrero es obligatorio")
        @Size(max = 100)
        private String name;

        private String code;
        private BigDecimal areaHectares;
        private BigDecimal hectares;
        private String forageType;
        /** Capacidad máxima medida en número de animales. */
        private Integer capacity;
        private String status;
    }

    @Data
    public static class UpdateRequest {
        @Size(max = 100)
        private String name;
        private String code;
        private BigDecimal areaHectares;
        private BigDecimal hectares;
        private String forageType;
        /** Capacidad máxima medida en número de animales. */
        private Integer capacity;
        private String status;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID farmId;
        private String farmName;
        private String name;
        private String code;
        private BigDecimal areaHectares;
        private BigDecimal hectares;
        private String forageType;
        /** Capacidad máxima medida en número de animales. */
        private Integer capacity;
        private String status;
        /** Animales activos que hoy pastan en el potrero (derivado de sus lotes). */
        private Integer currentAnimals;
        /** Capacidad todavía disponible: capacidad máxima menos ocupación. */
        private Integer availableCapacity;
        /** Porcentaje de ocupación (0-100) redondeado. */
        private Integer occupancyPercentage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
