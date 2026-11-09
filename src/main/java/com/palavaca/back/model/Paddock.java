package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "paddocks", indexes = {
        @Index(name = "idx_paddocks_farm_id", columnList = "farm_id"),
        @Index(name = "idx_paddocks_code", columnList = "code"),
        @Index(name = "idx_paddocks_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paddock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    @Builder.Default
    private Long version = 0L;

    @NotNull(message = "La finca es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @NotBlank(message = "El nombre o número del potrero es obligatorio")
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 50)
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "area_hectares", precision = 10, scale = 2)
    private BigDecimal areaHectares;

    @Size(max = 100)
    @Column(name = "forage_type", length = 100)
    private String forageType;

    @Column(name = "capacity_animal_units")
    private Integer capacityAnimalUnits;

    @Column(name = "capacity")
    private Integer capacity;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "available";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}