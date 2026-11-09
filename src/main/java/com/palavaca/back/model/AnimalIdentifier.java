package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "animal_identifiers", indexes = {
        @Index(name = "idx_animal_identifiers_animal_id", columnList = "animal_id"),
        @Index(name = "idx_animal_identifiers_value", columnList = "identifier")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimalIdentifier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @NotBlank
    @Size(max = 50)
    @Column(name = "identifier_type", nullable = false, length = 50)
    private String identifierType;

    @NotBlank
    @Size(max = 100)
    @Column(name = "identifier", nullable = false, length = 100)
    private String identifier;

    @Builder.Default
    @Column(name = "is_current", nullable = false)
    private boolean current = true;

    @Builder.Default
    @Column(name = "is_official", nullable = false)
    private boolean official = false;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
