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
@Table(name = "health_records", indexes = {
        @Index(name = "idx_health_records_animal_id", columnList = "animal_id"),
        @Index(name = "idx_health_records_medication_id", columnList = "medication_id"),
        @Index(name = "idx_health_records_event_date", columnList = "event_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    @Builder.Default
    private Long version = 0L;

    @NotNull(message = "El animal es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    private Medication medication;

    @NotNull(message = "La fecha del evento es obligatoria")
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Size(max = 50)
    @Column(name = "event_type", length = 50)
    private String eventType; // Vaccination, Deworming, Treatment, Checkup

    @Size(max = 150)
    @Column(name = "diagnosis", length = 150)
    private String diagnosis;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Size(max = 150)
    @Column(name = "veterinarian_name", length = 150)
    private String veterinarianName;

    @Column(name = "dosage", precision = 10, scale = 2)
    private BigDecimal dosage;

    @Size(max = 30)
    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "withdrawal_end_date")
    private LocalDate withdrawalEndDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}