package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sales_records", indexes = {
        @Index(name = "idx_sales_organization_id", columnList = "organization_id"),
        @Index(name = "idx_sales_customer_id", columnList = "customer_id"),
        @Index(name = "idx_sales_animal_id", columnList = "animal_id"),
        @Index(name = "idx_sales_sale_date", columnList = "sale_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "La organización es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @NotNull(message = "La fecha de venta es obligatoria")
    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Size(max = 50)
    @Column(name = "concept", length = 50) // Animal, Milk, Product, Other
    private String concept;

    @Column(name = "quantity", precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "El monto total es obligatorio")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Size(max = 30)
    @Column(name = "payment_status", length = 30) // Paid, Pending, Cancelled
    private String paymentStatus;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}