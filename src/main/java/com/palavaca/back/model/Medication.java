package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medications", indexes = {
        @Index(name = "idx_medications_name", columnList = "name"),
        @Index(name = "idx_medications_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "El nombre del medicamento es obligatorio")
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 100)
    @Column(name = "active_ingredient", length = 100)
    private String activeIngredient;

    @Size(max = 100)
    @Column(name = "category", length = 100)
    private String category;

    @Size(max = 50)
    @Column(name = "presentation", length = 50)
    private String presentation;

    @Size(max = 30)
    @Column(name = "default_unit", length = 30)
    private String defaultUnit;

    @Column(name = "withdrawal_days_milk")
    private Integer withdrawalDaysMilk;

    @Column(name = "withdrawal_days_meat")
    private Integer withdrawalDaysMeat;

    @Column(name = "default_dosage", precision = 10, scale = 2)
    private BigDecimal defaultDosage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}