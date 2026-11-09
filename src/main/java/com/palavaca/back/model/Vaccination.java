package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vaccinations", indexes = {
        @Index(name = "idx_vaccinations_animal_id", columnList = "animal_id"),
        @Index(name = "idx_vaccinations_due_date", columnList = "next_due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vaccination {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @NotBlank
    @Size(max = 150)
    @Column(name = "vaccine_name", nullable = false, length = 150)
    private String vaccineName;

    @Size(max = 150)
    @Column(name = "manufacturer", length = 150)
    private String manufacturer;

    @NotNull
    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @Column(name = "dose", precision = 10, scale = 3)
    private BigDecimal dose;

    @Size(max = 30)
    @Column(name = "dose_unit", length = 30)
    private String doseUnit;

    @Size(max = 150)
    @Column(name = "veterinarian_name", length = 150)
    private String veterinarianName;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
