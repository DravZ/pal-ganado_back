package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_movements", indexes = {
        @Index(name = "idx_inv_movements_inventory_id", columnList = "inventory_id"),
        @Index(name = "idx_inv_movements_type", columnList = "movement_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "El registro de inventario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Size(max = 30)
    @Column(name = "movement_type", nullable = false, length = 30) // IN, OUT, ADJUSTMENT
    private String movementType;

    @NotNull(message = "La cantidad es obligatoria")
    @Column(name = "quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Size(max = 100)
    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}