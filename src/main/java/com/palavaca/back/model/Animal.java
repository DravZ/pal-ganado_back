package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "animals", indexes = {
        @Index(name = "idx_animals_organization_id", columnList = "organization_id"),
        @Index(name = "idx_animals_farm_id", columnList = "farm_id"),
        @Index(name = "idx_animals_paddock_id", columnList = "paddock_id"),
        @Index(name = "idx_animals_tag_number", columnList = "tag_number"),
        @Index(name = "idx_animals_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "La organización es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id")
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paddock_id")
    private Paddock paddock;

    @NotBlank(message = "El arete o número de identificación es obligatorio")
    @Size(max = 50)
    @Column(name = "tag_number", nullable = false, length = 50)
    private String tagNumber;

    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;

    @NotBlank(message = "El género es obligatorio (M/F)")
    @Size(max = 10)
    @Column(name = "gender", nullable = false, length = 10)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Size(max = 100)
    @Column(name = "breed", length = 100)
    private String breed;

    @Size(max = 50)
    @Column(name = "category", length = 50)
    private String category;

    @Size(max = 50)
    @Column(name = "purpose", length = 50)
    private String purpose;

    @Column(name = "weight_at_birth", precision = 8, scale = 2)
    private BigDecimal weightAtBirth;

    @Column(name = "current_weight", precision = 8, scale = 2)
    private BigDecimal currentWeight;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "active";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sire_id")
    private Animal sire; // Padre (toro)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dam_id")
    private Animal dam; // Madre (vaca)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}