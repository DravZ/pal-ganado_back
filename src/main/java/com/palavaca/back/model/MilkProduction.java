package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "milk_productions", indexes = {
        @Index(name = "idx_milk_productions_animal_id", columnList = "animal_id"),
        @Index(name = "idx_milk_productions_record_date", columnList = "record_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilkProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "El animal es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @NotNull(message = "La fecha del registro es obligatoria")
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "morning_liters", precision = 8, scale = 2)
    private BigDecimal morningLiters;

    @Column(name = "afternoon_liters", precision = 8, scale = 2)
    private BigDecimal afternoonLiters;

    @NotNull(message = "El total de litros es obligatorio")
    @Column(name = "total_liters", nullable = false, precision = 8, scale = 2)
    private BigDecimal totalLiters;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}