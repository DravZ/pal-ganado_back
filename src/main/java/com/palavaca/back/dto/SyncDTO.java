package com.palavaca.back.dto;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SyncDTO {

    private SyncDTO() {}

    public record BootstrapResponse(
            UUID userId,
            UUID organizationId,
            List<UUID> farmIds,
            long cursor,
            Instant snapshotAt,
            Map<String, List<Map<String, Object>>> tables) {}

    public record Change(
            long sequence,
            String resourceType,
            UUID recordId,
            String operation,
            JsonNode payload,
            Instant occurredAt) {}

    public record ChangePage(
            UUID userId,
            long cursor,
            long upperBound,
            long nextCursor,
            boolean hasMore,
            List<Change> changes) {}
}
