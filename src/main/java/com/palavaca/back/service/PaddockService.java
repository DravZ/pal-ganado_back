package com.palavaca.back.service;

import com.palavaca.back.dto.PaddockDTO;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.Paddock;
import com.palavaca.back.repository.AnimalRepository;
import com.palavaca.back.repository.FarmRepository;
import com.palavaca.back.repository.LotRepository;
import com.palavaca.back.repository.PaddockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaddockService {

    private final PaddockRepository paddockRepository;
    private final FarmRepository farmRepository;
    private final AnimalRepository animalRepository;
    private final LotRepository lotRepository;

    @Transactional(readOnly = true)
    public List<PaddockDTO.Response> findByFarmId(UUID farmId) {
        Map<UUID, Long> occupancy = new HashMap<>();
        for (Object[] row : animalRepository.countActiveByPaddockForFarm(farmId)) {
            occupancy.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return paddockRepository.findByFarmId(farmId).stream()
                .map(paddock -> mapToResponse(paddock, occupancy.getOrDefault(paddock.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public PaddockDTO.Response findById(UUID id) {
        Paddock paddock = paddockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Potrero no encontrado con ID: " + id));
        return mapToResponse(paddock, animalRepository.countActiveByPaddockId(id));
    }

    @Transactional
    public PaddockDTO.Response create(PaddockDTO.CreateRequest request) {
        Farm farm = farmRepository.findById(request.getFarmId())
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada con ID: " + request.getFarmId()));

        Paddock paddock = Paddock.builder()
                .farm(farm)
                .name(request.getName())
                .code(request.getCode())
                .areaHectares(request.getHectares() != null ? request.getHectares() : request.getAreaHectares())
                .forageType(request.getForageType())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : "available")
                .build();

        return mapToResponse(paddockRepository.save(paddock), 0L);
    }

    @Transactional
    public PaddockDTO.Response update(UUID id, PaddockDTO.UpdateRequest request) {
        Paddock paddock = paddockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Potrero no encontrado con ID: " + id));

        if (request.getName() != null) paddock.setName(request.getName());
        if (request.getCode() != null) paddock.setCode(request.getCode());
        if (request.getAreaHectares() != null) paddock.setAreaHectares(request.getAreaHectares());
        if (request.getHectares() != null) paddock.setAreaHectares(request.getHectares());
        if (request.getForageType() != null) paddock.setForageType(request.getForageType());
        if (request.getStatus() != null) paddock.setStatus(request.getStatus());

        if (request.getCapacity() != null) {
            long occupied = animalRepository.countActiveByPaddockId(id);
            if (request.getCapacity() > 0 && occupied > request.getCapacity()) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "No puedes reducir la capacidad a " + request.getCapacity()
                                + " animales: el potrero tiene " + occupied + " animales activos.");
            }
            paddock.setCapacity(request.getCapacity());
        }

        return mapToResponse(paddockRepository.save(paddock), animalRepository.countActiveByPaddockId(id));
    }

    @Transactional
    public void delete(UUID id) {
        Paddock paddock = paddockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Potrero no encontrado con ID: " + id));
        if (lotRepository.countByCurrentPaddockId(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "No puedes eliminar un potrero con lotes asignados. Traslada sus lotes primero.");
        }
        paddock.setDeletedAt(LocalDateTime.now());
        paddockRepository.save(paddock);
    }

    private PaddockDTO.Response mapToResponse(Paddock p, long occupancy) {
        Integer capacity = p.getCapacity();
        Integer available = capacity == null ? null : Math.max(0, capacity - (int) occupancy);
        Integer percentage = capacity != null && capacity > 0
                ? (int) Math.min(100, Math.round(occupancy * 100.0 / capacity))
                : null;
        return PaddockDTO.Response.builder()
                .id(p.getId())
                .farmId(p.getFarm().getId())
                .farmName(p.getFarm().getName())
                .name(p.getName())
                .code(p.getCode())
                .areaHectares(p.getAreaHectares())
                .hectares(p.getAreaHectares())
                .forageType(p.getForageType())
                .capacity(capacity)
                .status(p.getStatus())
                .currentAnimals((int) occupancy)
                .availableCapacity(available)
                .occupancyPercentage(percentage)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
