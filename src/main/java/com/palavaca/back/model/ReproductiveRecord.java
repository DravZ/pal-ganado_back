package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reproductive_records", indexes = {
        @Index(name = "idx_rep_records_animal_id", columnList = "animal_id"),
        @Index(name = "idx_rep_records_event_date", columnList = "event_date"),
        @Index(name = "idx_rep_records_event_type", columnList = "event_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReproductiveRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    @Builder.Default
    private Long version = 0L;

    @NotNull(message = "El animal (hembra) es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @NotNull(message = "La fecha del evento es obligatoria")
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @NotNull(message = "El tipo de evento es obligatorio")
    @Size(max = 50)
    @Column(name = "event_type", nullable = false, length = 50) // Heat, Insemination, PregnancyCheck, Birth, Abort
    private String eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sire_id")
    private Animal sire; // Toro utilizado para la monta/inseminación

    @Column(name = "expected_birth_date")
    private LocalDate expectedBirthDate;

    @Size(max = 50)
    @Column(name = "pregnancy_status", length = 50) // Pregnant, Empty, Suspect
    private String pregnancyStatus;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}