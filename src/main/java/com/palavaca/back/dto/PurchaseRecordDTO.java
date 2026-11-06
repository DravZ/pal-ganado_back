package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PurchaseRecordDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de la organización es obligatorio")
        private UUID organizationId;

        private UUID supplierId;
        private UUID productId;
        private UUID medicationId;

        @NotNull(message = "La fecha de compra es obligatoria")
        private LocalDate purchaseDate;

        private BigDecimal quantity;
        private BigDecimal unitPrice;

        @NotNull(message = "El monto total es obligatorio")
        private BigDecimal totalAmount;

        private String paymentStatus;
        private String notes;
    }

    @Data
    public static class UpdateRequest {
        private UUID supplierId;
        private LocalDate purchaseDate;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private String paymentStatus;
        private String notes;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID organizationId;
        private UUID supplierId;
        private String supplierName;
        private UUID productId;
        private String productName;
        private UUID medicationId;
        private String medicationName;
        private LocalDate purchaseDate;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private String paymentStatus;
        private String notes;
        private LocalDateTime createdAt;
    }
}