package com.palavaca.back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public class PurchaseRecordDTO {

    @Data
    public static class CreateRequest {
        private UUID organizationId;

        private UUID farmId;
        private String invoiceNumber;
        private UUID supplierId;
        private UUID productId;
        private UUID medicationId;

        @NotNull(message = "La fecha de compra es obligatoria")
        private LocalDate purchaseDate;

        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal total;

        private BigDecimal totalAmount;

        private String paymentStatus;
        private String notes;
        private List<LineRequest> lines;
    }

    @Data
    public static class UpdateRequest {
        private UUID farmId;
        private String invoiceNumber;
        private UUID supplierId;
        private LocalDate purchaseDate;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal total;
        private BigDecimal totalAmount;
        private String paymentStatus;
        private String notes;
        private List<LineRequest> lines;
    }

    @Data
    public static class LineRequest {
        private UUID productId;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID organizationId;
        private UUID farmId;
        private String invoiceNumber;
        private UUID supplierId;
        private String supplierName;
        private UUID productId;
        private String productName;
        private UUID medicationId;
        private String medicationName;
        private LocalDate purchaseDate;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal total;
        private BigDecimal totalAmount;
        private String paymentStatus;
        private String notes;
        private List<LineResponse> lines;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class LineResponse {
        private UUID id;
        private UUID productId;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}