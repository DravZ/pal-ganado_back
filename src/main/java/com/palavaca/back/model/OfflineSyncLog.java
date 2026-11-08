package com.palavaca.back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offline_sync_logs", indexes = {
        @Index(name = "idx_sync_user_id", columnList = "user_id"),
        @Index(name = "idx_sync_status", columnList = "sync_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "La entidad objetivo es obligatoria")
    @Size(max = 100)
    @Column(name = "entity_name", nullable = false, length = 100) // HealthRecord, MilkProduction, etc.
    private String entityName;

    @NotBlank(message = " La acción es obligatoria")
    @Size(max = 30)
    @Column(name = "action_type", nullable = false, length = 30) // CREATE, UPDATE, DELETE
    private String actionType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload; // JSON con los datos capturados offline

    @Builder.Default
    @Size(max = 30)
    @Column(name = "sync_status", nullable = false, length = 30) // PENDING, PROCESSED, FAILED
    private String syncStatus = "PENDING";

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}