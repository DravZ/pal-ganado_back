package com.palavaca.back.controller;

import com.palavaca.back.dto.FrontendResourceDTO.*;
import com.palavaca.back.model.*;
import com.palavaca.back.repository.*;
import com.palavaca.back.service.HatoAuthorizationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FrontendResourcesController {

    private final LotRepository lotRepository;
    private final FarmRepository farmRepository;
    private final PaddockRepository paddockRepository;
    private final AnimalRepository animalRepository;
    private final AnimalIdentifierRepository identifierRepository;
    private final TreatmentRepository treatmentRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final MedicationRepository medicationRepository;
    private final ProductRepository productRepository;
    private final VaccinationRepository vaccinationRepository;
    private final FeedingRecordRepository feedingRepository;
    private final ProductionRecordRepository productionRepository;
    private final ExpenseRepository expenseRepository;
    private final CostAllocationRepository allocationRepository;
    private final SupplierRepository supplierRepository;
    private final MovementRepository movementRepository;
    private final MovementAnimalRepository movementAnimalRepository;
    private final AlertRepository alertRepository;
    private final HatoAuthorizationService authorization;

    @GetMapping("/lots")
    public List<LotResponse> getLots(Authentication authentication, @RequestParam("farm_id") UUID farmId) {
        authorization.requireFarm(authentication, farmId);
        return lotRepository.findByFarmId(farmId).stream().map(this::lotResponse).toList();
    }

    @PostMapping("/lots")
    public ResponseEntity<LotResponse> createLot(
            Authentication authentication,
            @Valid @RequestBody LotRequest request) {
        Farm farm = authorization.requireFarm(authentication, request.farmId());
        Paddock currentPaddock = request.currentPaddockId() == null
                ? null
                : authorization.requirePaddock(authentication, request.currentPaddockId());
        if (currentPaddock != null
                && !farm.getId().equals(currentPaddock.getFarm().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Lot lot = Lot.builder()
                .farm(farm)
                .name(request.name())
                .code(request.code())
                .lotType(request.lotType())
                .purpose(request.purpose())
                .currentPaddock(currentPaddock)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(request.status() == null ? "active" : request.status())
                .notes(request.notes())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(lotResponse(lotRepository.save(lot)));
    }

    @GetMapping("/animal-identifiers")
    public List<IdentifierResponse> getIdentifiers(Authentication authentication, @RequestParam("animal_id") UUID animalId) {
        authorization.requireAnimal(authentication, animalId);
        return identifierRepository.findByAnimalId(animalId).stream().map(this::identifierResponse).toList();
    }

    @PostMapping("/animal-identifiers")
    public ResponseEntity<IdentifierResponse> createIdentifier(
            Authentication authentication,
            @Valid @RequestBody IdentifierRequest request) {
        Animal animal = authorization.requireAnimal(authentication, request.animalId());
        String normalized = request.identifier().trim().toLowerCase(Locale.ROOT);
        UUID organizationId = animal.getOrganization().getId();
        if (animalRepository.existsIdentifier(organizationId, normalized, null)
                || identifierRepository.existsIdentifier(organizationId, normalized, null)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El identificador \"" + request.identifier() + "\" ya está registrado en esta organización.");
        }
        AnimalIdentifier identifier = AnimalIdentifier.builder()
                .animal(animal)
                .identifierType(request.identifierType())
                .identifier(request.identifier())
                .current(request.isCurrent() == null || request.isCurrent())
                .official(Boolean.TRUE.equals(request.isOfficial()))
                .issueDate(request.issueDate())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(identifierResponse(identifierRepository.save(identifier)));
    }

    @GetMapping("/treatments")
    public List<TreatmentResponse> getTreatments(Authentication authentication, @RequestParam("animal_id") UUID animalId) {
        authorization.requireAnimal(authentication, animalId);
        return treatmentRepository.findByAnimalIdOrderByStartDateDesc(animalId).stream()
                .map(this::treatmentResponse).toList();
    }

    @PostMapping("/treatments")
    @Transactional
    public ResponseEntity<TreatmentResponse> createTreatment(
            Authentication authentication,
            @Valid @RequestBody TreatmentRequest request) {
        authorization.requireAnimal(authentication, request.animalId());
        HealthRecord healthRecord = request.healthEventId() == null ? null
                : healthRecordRepository.findById(request.healthEventId())
                .orElseThrow(() -> new EntityNotFoundException("Evento de salud no encontrado"));
        if (healthRecord != null
                && !healthRecord.getAnimal().getId().equals(request.animalId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Treatment treatment = Treatment.builder()
                .animal(requireAnimal(request.animalId()))
                .healthRecord(healthRecord)
                .medication(requireMedication(request.medicationId()))
                .startDate(request.startDate())
                .endDate(request.endDate())
                .dose(request.dose())
                .doseUnit(request.doseUnit())
                .frequency(request.frequency())
                .route(request.route())
                .withdrawalUntil(request.withdrawalUntil())
                .status(request.status() == null ? "ACTIVO" : request.status())
                .notes(request.notes())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(treatmentResponse(treatmentRepository.save(treatment)));
    }

    @GetMapping("/vaccinations")
    public List<VaccinationResponse> getVaccinations(
            Authentication authentication,
            @RequestParam("animal_id") UUID animalId) {
        authorization.requireAnimal(authentication, animalId);
        return vaccinationRepository.findByAnimalIdOrderByApplicationDateDesc(animalId).stream()
                .map(this::vaccinationResponse).toList();
    }

    @PostMapping("/vaccinations")
    public ResponseEntity<VaccinationResponse> createVaccination(
            Authentication authentication,
            @Valid @RequestBody VaccinationRequest request) {
        authorization.requireAnimal(authentication, request.animalId());
        Vaccination vaccination = Vaccination.builder()
                .animal(requireAnimal(request.animalId()))
                .vaccineName(request.vaccineName())
                .manufacturer(request.manufacturer())
                .applicationDate(request.applicationDate())
                .nextDueDate(request.nextDueDate())
                .dose(request.dose())
                .doseUnit(request.doseUnit())
                .veterinarianName(request.veterinarianName())
                .notes(request.notes())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vaccinationResponse(vaccinationRepository.save(vaccination)));
    }

    @GetMapping("/feeding-records")
    public List<FeedingResponse> getFeedingRecords(@RequestParam("farm_id") UUID farmId) {
        return feedingRepository.findByFarmIdOrderByFeedingDateDesc(farmId).stream()
                .map(this::feedingResponse).toList();
    }

    @PostMapping("/feeding-records")
    @Transactional
    public ResponseEntity<FeedingResponse> createFeedingRecord(@Valid @RequestBody FeedingRequest request) {
        Product product = request.productId() == null ? null : requireProduct(request.productId());
        if (product != null && request.quantity() != null) {
            BigDecimal available = product.getCurrentStock() == null ? BigDecimal.ZERO : product.getCurrentStock();
            if (available.compareTo(request.quantity()) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Stock insuficiente de " + product.getName() + ". Disponible: " + available + ".");
            }
            product.setCurrentStock(available.subtract(request.quantity()));
            productRepository.save(product);
        }
        FeedingRecord feeding = FeedingRecord.builder()
                .farm(requireFarm(request.farmId()))
                .lot(request.lotId() == null ? null : requireLot(request.lotId()))
                .animal(request.animalId() == null ? null : requireAnimal(request.animalId()))
                .product(product)
                .feedingDate(request.feedingDate())
                .quantity(request.quantity())
                .unit(request.unit())
                .cost(request.cost())
                .notes(request.notes())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedingResponse(feedingRepository.save(feeding)));
    }

    @GetMapping("/production-records")
    public List<ProductionResponse> getProductionRecords(@RequestParam("farm_id") UUID farmId) {
        return productionRepository.findByFarmIdOrderByProductionDateDesc(farmId).stream()
                .map(this::productionResponse).toList();
    }

    @PostMapping("/production-records")
    public ResponseEntity<ProductionResponse> createProductionRecord(@Valid @RequestBody ProductionRequest request) {
        ProductionRecord production = ProductionRecord.builder()
                .farm(requireFarm(request.farmId()))
                .animal(request.animalId() == null ? null : requireAnimal(request.animalId()))
                .lot(request.lotId() == null ? null : requireLot(request.lotId()))
                .productionDate(request.productionDate())
                .productionType(request.productionType())
                .quantity(request.quantity())
                .unit(request.unit())
                .pricePerUnit(request.pricePerUnit())
                .revenue(request.revenue())
                .notes(request.notes())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productionResponse(productionRepository.save(production)));
    }

    @GetMapping("/expenses")
    public List<ExpenseResponse> getExpenses(Authentication authentication, @RequestParam("farm_id") UUID farmId) {
        authorization.requireFarm(authentication, farmId);
        return expenseRepository.findByFarmIdOrderByExpenseDateDesc(farmId).stream()
                .map(this::expenseResponse).toList();
    }

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(
            Authentication authentication,
            @Valid @RequestBody ExpenseRequest request) {
        Farm farm = authorization.requireFarm(authentication, request.farmId());
        Supplier supplier = request.supplierId() == null ? null : supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
        if (supplier != null
                && !supplier.getOrganization().getId().equals(farm.getOrganization().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Expense expense = Expense.builder()
                .farm(farm)
                .supplier(supplier)
                .expenseDate(request.expenseDate())
                .category(request.category())
                .description(request.description())
                .amount(request.amount())
                .referenceType(request.referenceType())
                .referenceId(request.referenceId())
                .status(request.status() == null ? "PENDING" : request.status())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseResponse(expenseRepository.save(expense)));
    }

    @GetMapping("/cost-allocations")
    public List<AllocationResponse> getCostAllocations(
            Authentication authentication,
            @RequestParam("farm_id") UUID farmId) {
        authorization.requireFarm(authentication, farmId);
        return allocationRepository.findByFarmIdOrderByAllocationDateDesc(farmId).stream()
                .map(this::allocationResponse).toList();
    }

    @PostMapping("/cost-allocations")
    public ResponseEntity<AllocationResponse> createCostAllocation(
            Authentication authentication,
            @Valid @RequestBody AllocationRequest request) {
        Farm farm = authorization.requireFarm(authentication, request.farmId());
        Expense expense = request.expenseId() == null ? null : expenseRepository.findById(request.expenseId())
                .orElseThrow(() -> new EntityNotFoundException("Gasto no encontrado"));
        if (expense != null && !farm.getId().equals(expense.getFarm().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Lot lot = request.lotId() == null ? null : authorization.requireLot(authentication, request.lotId());
        if (lot != null && !farm.getId().equals(lot.getFarm().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Animal animal = request.animalId() == null ? null : authorization.requireAnimal(authentication, request.animalId());
        if (animal != null && (animal.getFarm() == null || !farm.getId().equals(animal.getFarm().getId()))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        CostAllocation allocation = CostAllocation.builder()
                .farm(farm)
                .expense(expense)
                .animal(animal)
                .lot(lot)
                .allocationType(request.allocationType())
                .amount(request.amount())
                .allocationDate(request.allocationDate())
                .notes(request.notes())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(allocationResponse(allocationRepository.save(allocation)));
    }

    @GetMapping("/movements")
    public List<MovementResponse> getMovements(Authentication authentication, @RequestParam("farm_id") UUID farmId) {
        authorization.requireFarm(authentication, farmId);
        return movementRepository.findByFarmIdOrderByMovementDateDesc(farmId).stream()
                .map(this::movementResponse).toList();
    }

    @PostMapping("/movements")
    @Transactional
    public ResponseEntity<MovementResponse> createMovement(
            Authentication authentication,
            @Valid @RequestBody MovementRequest request) {
        authorization.requireFarm(authentication, request.farmId());
        if (request.originLotId() != null
                && !request.farmId().equals(authorization.requireLot(authentication, request.originLotId()).getFarm().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (request.destinationLotId() != null
                && !request.farmId().equals(authorization.requireLot(authentication, request.destinationLotId()).getFarm().getId())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (request.animals() != null) {
            for (MovementAnimalRequest item : request.animals()) {
                Animal animal = authorization.requireAnimal(authentication, item.animalId());
                if (animal.getFarm() == null || !request.farmId().equals(animal.getFarm().getId())) {
                    throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
                }
            }
        }
        Movement movement = movementRepository.save(Movement.builder()
                .farm(requireFarm(request.farmId()))
                .originLot(request.originLotId() == null ? null : requireLot(request.originLotId()))
                .destinationLot(request.destinationLotId() == null ? null : requireLot(request.destinationLotId()))
                .movementDate(request.movementDate())
                .movementType(request.movementType())
                .reason(request.reason())
                .status(request.status() == null ? "COMPLETED" : request.status())
                .notes(request.notes())
                .build());
        if (request.animals() != null) {
            for (MovementAnimalRequest item : request.animals()) {
                Animal animal = requireAnimal(item.animalId());
                if (movement.getDestinationLot() != null) animal.setLot(movement.getDestinationLot());
                movementAnimalRepository.save(MovementAnimal.builder()
                        .movement(movement).animal(animal).weight(item.weight()).build());
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(movementResponse(movement));
    }

    @GetMapping("/alerts")
    public List<AlertResponse> getAlerts(@RequestParam("farm_id") UUID farmId) {
        return alertRepository.findByFarmIdOrderByDetectedAtDesc(farmId).stream()
                .map(this::alertResponse).toList();
    }

    @PostMapping("/alerts")
    public ResponseEntity<AlertResponse> createAlert(@Valid @RequestBody AlertRequest request) {
        Alert alert = Alert.builder()
                .farm(requireFarm(request.farmId()))
                .animal(request.animalId() == null ? null : requireAnimal(request.animalId()))
                .lot(request.lotId() == null ? null : requireLot(request.lotId()))
                .alertType(request.alertType())
                .severity(request.severity())
                .title(request.title())
                .message(request.message())
                .detectedAt(request.detectedAt())
                .dueDate(request.dueDate())
                .status(request.status() == null ? "open" : request.status())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(alertResponse(alertRepository.save(alert)));
    }

    @PatchMapping("/alerts/{id}")
    public AlertResponse updateAlertStatus(@PathVariable UUID id, @RequestBody AlertStatusRequest request) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alerta no encontrada"));
        alert.setStatus(request.status());
        if ("resolved".equalsIgnoreCase(request.status())) alert.setResolvedAt(LocalDateTime.now());
        return alertResponse(alertRepository.save(alert));
    }

    public record AlertStatusRequest(String status) {}

    private Farm requireFarm(UUID id) {
        return farmRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Finca no encontrada"));
    }

    private Paddock requirePaddock(UUID id) {
        return paddockRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Potrero no encontrado"));
    }

    private Lot requireLot(UUID id) {
        return lotRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Lote no encontrado"));
    }

    private Animal requireAnimal(UUID id) {
        return animalRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Animal no encontrado"));
    }

    private Medication requireMedication(UUID id) {
        return medicationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Medicamento no encontrado"));
    }

    private Product requireProduct(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
    }

    private LotResponse lotResponse(Lot lot) {
        return new LotResponse(lot.getId(), lot.getFarm().getId(), lot.getName(), lot.getCode(),
                lot.getLotType(), lot.getPurpose(),
                lot.getCurrentPaddock() == null ? null : lot.getCurrentPaddock().getId(),
                lot.getStartDate(), lot.getEndDate(), lot.getStatus(), lot.getNotes(),
                lot.getCreatedAt(), lot.getUpdatedAt());
    }

    private IdentifierResponse identifierResponse(AnimalIdentifier item) {
        return new IdentifierResponse(item.getId(), item.getAnimal().getId(), item.getIdentifierType(),
                item.getIdentifier(), item.isCurrent(), item.isOfficial(), item.getIssueDate(), item.getCreatedAt());
    }

    private TreatmentResponse treatmentResponse(Treatment item) {
        return new TreatmentResponse(item.getId(), item.getAnimal().getId(),
                item.getHealthRecord() == null ? null : item.getHealthRecord().getId(),
                item.getMedication().getId(), item.getStartDate(), item.getEndDate(), item.getDose(),
                item.getDoseUnit(), item.getFrequency(), item.getRoute(), item.getWithdrawalUntil(),
                item.getStatus(), item.getNotes(), item.getCreatedAt());
    }

    private VaccinationResponse vaccinationResponse(Vaccination item) {
        return new VaccinationResponse(item.getId(), item.getAnimal().getId(), item.getVaccineName(),
                item.getManufacturer(), item.getApplicationDate(), item.getNextDueDate(), item.getDose(),
                item.getDoseUnit(), item.getVeterinarianName(), item.getNotes(), item.getCreatedAt());
    }

    private FeedingResponse feedingResponse(FeedingRecord item) {
        return new FeedingResponse(item.getId(), item.getFarm().getId(),
                item.getLot() == null ? null : item.getLot().getId(),
                item.getAnimal() == null ? null : item.getAnimal().getId(),
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getFeedingDate(), item.getQuantity(), item.getUnit(), item.getCost(),
                item.getNotes(), item.getCreatedAt());
    }

    private ProductionResponse productionResponse(ProductionRecord item) {
        return new ProductionResponse(item.getId(), item.getFarm().getId(),
                item.getAnimal() == null ? null : item.getAnimal().getId(),
                item.getLot() == null ? null : item.getLot().getId(), item.getProductionDate(),
                item.getProductionType(), item.getQuantity(), item.getUnit(), item.getPricePerUnit(),
                item.getRevenue(), item.getNotes(), item.getCreatedAt());
    }

    private ExpenseResponse expenseResponse(Expense item) {
        return new ExpenseResponse(item.getId(), item.getFarm().getId(),
                item.getSupplier() == null ? null : item.getSupplier().getId(), item.getExpenseDate(),
                item.getCategory(), item.getDescription(), item.getAmount(), item.getReferenceType(),
                item.getReferenceId(), item.getStatus(), item.getCreatedAt());
    }

    private AllocationResponse allocationResponse(CostAllocation item) {
        return new AllocationResponse(item.getId(), item.getFarm().getId(),
                item.getExpense() == null ? null : item.getExpense().getId(),
                item.getAnimal() == null ? null : item.getAnimal().getId(),
                item.getLot() == null ? null : item.getLot().getId(), item.getAllocationType(),
                item.getAmount(), item.getAllocationDate(), item.getNotes(), item.getCreatedAt());
    }

    private MovementResponse movementResponse(Movement item) {
        List<MovementAnimalRequest> animals = movementAnimalRepository.findByMovementId(item.getId()).stream()
                .map(link -> new MovementAnimalRequest(link.getAnimal().getId(), link.getWeight())).toList();
        return new MovementResponse(item.getId(), item.getFarm().getId(),
                item.getOriginLot() == null ? null : item.getOriginLot().getId(),
                item.getDestinationLot() == null ? null : item.getDestinationLot().getId(),
                item.getMovementDate(), item.getMovementType(), item.getReason(), item.getStatus(),
                item.getNotes(), animals, item.getCreatedAt());
    }

    private AlertResponse alertResponse(Alert item) {
        return new AlertResponse(item.getId(), item.getFarm().getId(),
                item.getAnimal() == null ? null : item.getAnimal().getId(),
                item.getLot() == null ? null : item.getLot().getId(), item.getAlertType(),
                item.getSeverity(), item.getTitle(), item.getMessage(), item.getDetectedAt(),
                item.getDueDate(), item.getStatus(), item.getResolvedAt(),
                item.getResolvedBy() == null ? null : item.getResolvedBy().getId(), item.getCreatedAt());
    }
}
