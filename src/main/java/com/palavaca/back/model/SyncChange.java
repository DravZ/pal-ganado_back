package com.palavaca.back.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_changes", indexes = {
        @Index(name = "idx_sync_changes_scope", columnList = "organization_id, farm_id, id"),
        @Index(name = "idx_sync_changes_record", columnList = "resource_type, record_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "farm_id")
    private UUID farmId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "resource_type", nullable = false, length = 80)
    private String resourceType;

    @Column(name = "record_id", nullable = false)
    private UUID recordId;

    @Column(name = "operation", nullable = false, length = 10)
    private String operation;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;
}
