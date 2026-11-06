package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class SalesRecordDTO {

    @Data
    public static class CreateRequest {
        @NotNull(message = "El ID de la organización es obligatorio")
        private UUID organizationId;

        private UUID customerId;
        private UUID animalId;
        private UUID productId;

        @NotNull(message = "La fecha de venta es obligatoria")
        private LocalDate saleDate;

        @Size(max = 50)
        private String concept;

        private BigDecimal quantity;
        private BigDecimal unitPrice;

        @NotNull(message = "El monto total es obligatorio")
        private BigDecimal totalAmount;

        private String paymentStatus;
        private String notes;
    }

    @Data
    public static class UpdateRequest {
        private UUID customerId;
        private LocalDate saleDate;
        private String concept;
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
        private UUID customerId;
        private String customerName;
        private UUID animalId;
        private String animalTagNumber;
        private UUID productId;
        private String productName;
        private LocalDate saleDate;
        private String concept;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private String paymentStatus;
        private String notes;
        private LocalDateTime createdAt;
    }
}