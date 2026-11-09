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
@Table(name = "production_records", indexes = {
        @Index(name = "idx_production_records_farm_id", columnList = "farm_id"),
        @Index(name = "idx_production_records_date", columnList = "production_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private Lot lot;

    @NotNull
    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @NotNull
    @Size(max = 50)
    @Column(name = "production_type", nullable = false, length = 50)
    private String productionType;

    @NotNull
    @Column(name = "quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity;

    @Size(max = 30)
    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "price_per_unit", precision = 14, scale = 4)
    private BigDecimal pricePerUnit;

    @Column(name = "revenue", precision = 14, scale = 2)
    private BigDecimal revenue;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
