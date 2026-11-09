package com.palavaca.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class FrontendResourceDTO {

    private FrontendResourceDTO() {}

    public record LotRequest(
            @NotNull UUID farmId,
            @NotBlank String name,
            String code,
            String lotType,
            String purpose,
            UUID currentPaddockId,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            String notes) {}

    public record LotResponse(
            UUID id, UUID farmId, String name, String code, String lotType, String purpose,
            UUID currentPaddockId, LocalDate startDate, LocalDate endDate, String status,
            String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {}

    public record IdentifierRequest(
            @NotNull UUID animalId,
            @NotBlank String identifierType,
            @NotBlank String identifier,
            Boolean isCurrent,
            Boolean isOfficial,
            LocalDate issueDate) {}

    public record IdentifierResponse(
            UUID id, UUID animalId, String identifierType, String identifier,
            boolean isCurrent, boolean isOfficial, LocalDate issueDate, LocalDateTime createdAt) {}

    public record TreatmentRequest(
            @NotNull UUID animalId, UUID healthEventId, @NotNull UUID medicationId,
            @NotNull LocalDateTime startDate, LocalDateTime endDate, BigDecimal dose,
            String doseUnit, String frequency, String route, LocalDate withdrawalUntil,
            String status, String notes) {}

    public record TreatmentResponse(
            UUID id, UUID animalId, UUID healthEventId, UUID medicationId,
            LocalDateTime startDate, LocalDateTime endDate, BigDecimal dose, String doseUnit,
            String frequency, String route, LocalDate withdrawalUntil, String status,
            String notes, LocalDateTime createdAt) {}

    public record VaccinationRequest(
            @NotNull UUID animalId, @NotBlank String vaccineName, String manufacturer,
            @NotNull LocalDateTime applicationDate, LocalDate nextDueDate, BigDecimal dose,
            String doseUnit, String veterinarianName, String notes) {}

    public record VaccinationResponse(
            UUID id, UUID animalId, String vaccineName, String manufacturer,
            LocalDateTime applicationDate, LocalDate nextDueDate, BigDecimal dose,
            String doseUnit, String veterinarianName, String notes, LocalDateTime createdAt) {}

    public record FeedingRequest(
            @NotNull UUID farmId, UUID lotId, UUID animalId, UUID productId,
            @NotNull LocalDate feedingDate, @NotNull BigDecimal quantity, String unit,
            BigDecimal cost, String notes) {}

    public record FeedingResponse(
            UUID id, UUID farmId, UUID lotId, UUID animalId, UUID productId,
            LocalDate feedingDate, BigDecimal quantity, String unit, BigDecimal cost,
            String notes, LocalDateTime createdAt) {}

    public record ProductionRequest(
            @NotNull UUID farmId, UUID animalId, UUID lotId, @NotNull LocalDate productionDate,
            @NotBlank String productionType, @NotNull BigDecimal quantity, String unit,
            BigDecimal pricePerUnit, BigDecimal revenue, String notes) {}

    public record ProductionResponse(
            UUID id, UUID farmId, UUID animalId, UUID lotId, LocalDate productionDate,
            String productionType, BigDecimal quantity, String unit, BigDecimal pricePerUnit,
            BigDecimal revenue, String notes, LocalDateTime createdAt) {}

    public record ExpenseRequest(
            @NotNull UUID farmId, UUID supplierId, @NotNull LocalDate expenseDate,
            String category, String description, @NotNull BigDecimal amount,
            String referenceType, UUID referenceId, String status) {}

    public record ExpenseResponse(
            UUID id, UUID farmId, UUID supplierId, LocalDate expenseDate, String category,
            String description, BigDecimal amount, String referenceType, UUID referenceId,
            String status, LocalDateTime createdAt) {}

    public record AllocationRequest(
            @NotNull UUID farmId, UUID expenseId, UUID animalId, UUID lotId,
            String allocationType, @NotNull BigDecimal amount, LocalDate allocationDate,
            String notes) {}

    public record AllocationResponse(
            UUID id, UUID farmId, UUID expenseId, UUID animalId, UUID lotId,
            String allocationType, BigDecimal amount, LocalDate allocationDate,
            String notes, LocalDateTime createdAt) {}

    public record MovementAnimalRequest(@NotNull UUID animalId, BigDecimal weight) {}

    public record MovementRequest(
            @NotNull UUID farmId, UUID originLotId, UUID destinationLotId,
            @NotNull LocalDateTime movementDate, @NotBlank String movementType,
            String reason, String status, String notes, List<MovementAnimalRequest> animals) {}

    public record MovementResponse(
            UUID id, UUID farmId, UUID originLotId, UUID destinationLotId,
            LocalDateTime movementDate, String movementType, String reason, String status,
            String notes, List<MovementAnimalRequest> animals, LocalDateTime createdAt) {}

    public record AlertRequest(
            @NotNull UUID farmId, UUID animalId, UUID lotId, @NotBlank String alertType,
            String severity, @NotBlank String title, String message,
            @NotNull LocalDateTime detectedAt, LocalDateTime dueDate, String status) {}

    public record AlertResponse(
            UUID id, UUID farmId, UUID animalId, UUID lotId, String alertType, String severity,
            String title, String message, LocalDateTime detectedAt, LocalDateTime dueDate,
            String status, LocalDateTime resolvedAt, UUID resolvedBy, LocalDateTime createdAt) {}
}
