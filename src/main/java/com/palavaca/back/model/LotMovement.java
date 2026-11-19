package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Historial de traslados de un lote completo entre potreros.
 *
 * <p>Registra el potrero de origen (nulo cuando es la asignación inicial), el potrero de destino,
 * la fecha y hora del traslado, cuántos animales activos se movieron y el usuario responsable.</p>
 */
@Entity
@Table(name = "lot_movements", indexes = {
        @Index(name = "idx_lot_movements_farm_id", columnList = "farm_id"),
        @Index(name = "idx_lot_movements_lot_id", columnList = "lot_id"),
        @Index(name = "idx_lot_movements_date", columnList = "movement_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    @Builder.Default
    private Long version = 0L;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_paddock_id")
    private Paddock originPaddock;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_paddock_id", nullable = false)
    private Paddock destinationPaddock;

    @NotNull
    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;

    @Column(name = "animal_count")
    private Integer animalCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moved_by_id")
    private User movedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
