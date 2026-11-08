package com.palavaca.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class FarmDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de la organización es obligatorio")
        private UUID organizationId;

        @NotBlank(message = "El nombre de la finca es obligatorio")
        @Size(max = 150)
        private String name;

        private String code;
        private String state;
        private String municipality;
        private String locality;
        private String address;
        private String location;
        private BigDecimal totalAreaHectares;
        private BigDecimal hectares;
        private String status;
    }

    @Data
    public static class UpdateRequest {
        @Size(max = 150)
        private String name;
        private String code;
        private String state;
        private String municipality;
        private String locality;
        private String address;
        private String location;
        private BigDecimal totalAreaHectares;
        private BigDecimal hectares;
        private String status;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID organizationId;
        private String name;
        private String code;
        private String state;
        private String municipality;
        private String locality;
        private String address;
        private String location;
        private BigDecimal totalAreaHectares;
        private BigDecimal hectares;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}