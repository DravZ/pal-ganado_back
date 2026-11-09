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
@Table(name = "treatments", indexes = {
        @Index(name = "idx_treatments_animal_id", columnList = "animal_id"),
        @Index(name = "idx_treatments_health_record_id", columnList = "health_record_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_record_id")
    private HealthRecord healthRecord;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "dose", precision = 12, scale = 3)
    private BigDecimal dose;

    @Size(max = 30)
    @Column(name = "dose_unit", length = 30)
    private String doseUnit;

    @Size(max = 50)
    @Column(name = "frequency", length = 50)
    private String frequency;

    @Size(max = 50)
    @Column(name = "route", length = 50)
    private String route;

    @Column(name = "withdrawal_until")
    private LocalDate withdrawalUntil;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVO";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
