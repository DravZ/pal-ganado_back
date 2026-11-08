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
@Table(name = "products", indexes = {
        @Index(name = "idx_products_organization_id", columnList = "organization_id"),
        @Index(name = "idx_products_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "La organización es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 80)
    @Column(name = "sku", length = 80, unique = true)
    private String sku;

    @Size(max = 100)
    @Column(name = "category", length = 100)
    private String category;

    @Size(max = 80)
    @Column(name = "product_type", length = 80)
    private String productType;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(max = 30)
    @Column(name = "unit_of_measure", nullable = false, length = 30)
    private String unitOfMeasure;

    @Builder.Default
    @Column(name = "current_stock", nullable = false, precision = 14, scale = 3)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "minimum_stock", precision = 14, scale = 3)
    private BigDecimal minimumStock;

    @Column(name = "reorder_point", precision = 14, scale = 3)
    private BigDecimal reorderPoint;

    @Column(name = "average_cost", precision = 14, scale = 4)
    private BigDecimal averageCost;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "active";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}