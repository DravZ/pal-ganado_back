package com.palavaca.back.service;

import com.palavaca.back.dto.FrontendResourceDTO.LotRequest;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.Lot;
import com.palavaca.back.model.LotMovement;
import com.palavaca.back.model.Paddock;
import com.palavaca.back.model.User;
import com.palavaca.back.repository.AnimalRepository;
import com.palavaca.back.repository.FarmRepository;
import com.palavaca.back.repository.LotMovementRepository;
import com.palavaca.back.repository.LotRepository;
import com.palavaca.back.repository.PaddockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gestión de lotes.
 *
 * <p>Un lote es una agrupación de animales que siempre tiene un potrero actual. Cuando un lote cambia
 * de potrero se registra un {@link LotMovement} con el origen, el destino, la fecha y hora, cuántos
 * animales activos se movieron y el usuario responsable. La capacidad del potrero destino se valida
 * bajo bloqueo para no superar el máximo.</p>
 */
@Service
@RequiredArgsConstructor
public class LotService {

    private final LotRepository lotRepository;
    private final FarmRepository farmRepository;
    private final PaddockRepository paddockRepository;
    private final LotMovementRepository lotMovementRepository;
    private final AnimalRepository animalRepository;
    private final PaddockCapacityService capacityService;

    @Transactional
    public Lot create(LotRequest request, User movedBy) {
        Farm farm = farmRepository.findById(request.farmId())
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada con ID: " + request.farmId()));
        Paddock paddock = requirePaddock(request.currentPaddockId());
        if (!farm.getId().equals(paddock.getFarm().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El potrero seleccionado no pertenece a la finca del lote.");
        }

        Paddock lockedPaddock = capacityService.lock(paddock.getId());
        Lot lot = Lot.builder()
                .farm(farm)
                .name(request.name())
                .code(request.code())
                .lotType(request.lotType())
                .purpose(request.purpose())
                .currentPaddock(lockedPaddock)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(request.status() == null ? "active" : request.status())
                .notes(request.notes())
                .build();
        Lot saved = lotRepository.save(lot);
        capacityService.assertNotOverCapacity(lockedPaddock);

        // Asignación inicial: sin potrero de origen, deja constancia de dónde nace el lote.
        lotMovementRepository.save(LotMovement.builder()
                .farm(farm)
                .lot(saved)
                .originPaddock(null)
                .destinationPaddock(lockedPaddock)
                .movementDate(LocalDateTime.now())
                .animalCount(0)
                .movedBy(movedBy)
                .notes(request.movementNotes())
                .build());
        return saved;
    }

    @Transactional
    public Lot update(UUID id, LotRequest request, User movedBy) {
        Lot lot = lotRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Lote no encontrado con ID: " + id));

        if (request.name() != null) lot.setName(request.name());
        if (request.code() != null) lot.setCode(request.code());
        if (request.lotType() != null) lot.setLotType(request.lotType());
        if (request.purpose() != null) lot.setPurpose(request.purpose());
        if (request.startDate() != null) lot.setStartDate(request.startDate());
        if (request.endDate() != null) lot.setEndDate(request.endDate());
        if (request.status() != null) lot.setStatus(request.status());
        if (request.notes() != null) lot.setNotes(request.notes());

        if (request.currentPaddockId() != null) {
            Paddock destination = requirePaddock(request.currentPaddockId());
            Paddock origin = lot.getCurrentPaddock();
            boolean isTransfer = origin == null || !origin.getId().equals(destination.getId());
            if (isTransfer) {
                if (!lot.getFarm().getId().equals(destination.getFarm().getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "El potrero destino no pertenece a la finca del lote.");
                }
                Paddock lockedDestination = capacityService.lock(destination.getId());
                lot.setCurrentPaddock(lockedDestination);
                Lot saved = lotRepository.save(lot);
                capacityService.assertNotOverCapacity(lockedDestination);

                long animalCount = animalRepository.countActiveByLotId(id);
                lotMovementRepository.save(LotMovement.builder()
                        .farm(lot.getFarm())
                        .lot(saved)
                        .originPaddock(origin)
                        .destinationPaddock(lockedDestination)
                        .movementDate(LocalDateTime.now())
                        .animalCount((int) animalCount)
                        .movedBy(movedBy)
                        .notes(request.movementNotes())
                        .build());
                return saved;
            }
        }
        return lotRepository.save(lot);
    }

    @Transactional(readOnly = true)
    public long activeAnimals(UUID lotId) {
        return animalRepository.countActiveByLotId(lotId);
    }

    private Paddock requirePaddock(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debes seleccionar un potrero para el lote.");
        }
        return paddockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Potrero no encontrado con ID: " + id));
    }
}
