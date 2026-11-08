package com.palavaca.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de la organización es obligatorio")
        private UUID organizationId;

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 150)
        private String name;

        private String sku;
        private String category;
        private String productType;

        @Size(max = 30)
        private String unitOfMeasure;
        private String unit;
        private BigDecimal currentStock;
        private BigDecimal minimumStock;
        private BigDecimal reorderPoint;
        private BigDecimal averageCost;
        private String status;

        private String description;
    }

    @Data
    public static class UpdateRequest {
        @Size(max = 150)
        private String name;
        private String sku;
        private String category;
        private String productType;
        @Size(max = 30)
        private String unitOfMeasure;
        private String unit;
        private BigDecimal currentStock;
        private BigDecimal minimumStock;
        private BigDecimal reorderPoint;
        private BigDecimal averageCost;
        private String status;
        private String description;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID organizationId;
        private String name;
        private String sku;
        private String category;
        private String productType;
        private String unitOfMeasure;
        private String unit;
        private BigDecimal currentStock;
        private BigDecimal minimumStock;
        private BigDecimal reorderPoint;
        private BigDecimal averageCost;
        private String status;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}