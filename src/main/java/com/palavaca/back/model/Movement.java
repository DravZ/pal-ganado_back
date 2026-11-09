package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movements", indexes = {
        @Index(name = "idx_movements_farm_id", columnList = "farm_id"),
        @Index(name = "idx_movements_date", columnList = "movement_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_lot_id")
    private Lot originLot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_lot_id")
    private Lot destinationLot;

    @NotNull
    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;

    @NotNull
    @Size(max = 50)
    @Column(name = "movement_type", nullable = false, length = 50)
    private String movementType;

    @Size(max = 150)
    @Column(name = "reason", length = 150)
    private String reason;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "COMPLETED";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
