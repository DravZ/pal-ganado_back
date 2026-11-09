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
@Table(name = "financial_records", indexes = {
        @Index(name = "idx_financial_organization_id", columnList = "organization_id"),
        @Index(name = "idx_financial_record_date", columnList = "record_date"),
        @Index(name = "idx_financial_type", columnList = "transaction_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    @Builder.Default
    private Long version = 0L;

    @NotNull(message = "La organización es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id")
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @NotNull(message = "El tipo de transacción es obligatorio (INCOME/EXPENSE)")
    @Size(max = 20)
    @Column(name = "transaction_type", nullable = false, length = 20) // INCOME, EXPENSE
    private String transactionType;

    @Size(max = 100)
    @Column(name = "category", length = 100) // Feed, Medicine, Payroll, Sale, Maintenance, Fuel
    private String category;

    @NotNull(message = "El monto es obligatorio")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}