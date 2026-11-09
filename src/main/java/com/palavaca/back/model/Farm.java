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
@Table(name = "farms", indexes = {
        @Index(name = "idx_farms_organization_id", columnList = "organization_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    @Builder.Default
    private Long version = 0L;

    @NotNull(message = "La organización es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @NotBlank(message = "El nombre de la finca es obligatorio")
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 50)
    @Column(name = "code", length = 50)
    private String code;

    @Size(max = 100)
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 100)
    @Column(name = "municipality", length = 100)
    private String municipality;

    @Size(max = 100)
    @Column(name = "locality", length = 100)
    private String locality;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Size(max = 150)
    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "total_area_hectares", precision = 10, scale = 2)
    private BigDecimal totalAreaHectares;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "active";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}