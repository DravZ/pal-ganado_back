package com.palavaca.back.service;

import com.palavaca.back.dto.PaddockDTO;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.Paddock;
import com.palavaca.back.repository.FarmRepository;
import com.palavaca.back.repository.PaddockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaddockService {

    private final PaddockRepository paddockRepository;
    private final FarmRepository farmRepository;

    @Transactional(readOnly = true)
    public List<PaddockDTO.Response> findByFarmId(UUID farmId) {
        return paddockRepository.findByFarmId(farmId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaddockDTO.Response findById(UUID id) {
        Paddock paddock = paddockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Potrero no encontrado con ID: " + id));
        return mapToResponse(paddock);
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
                .capacityAnimalUnits(request.getCapacityAnimalUnits() != null
                        ? request.getCapacityAnimalUnits() : request.getCapacity())
                .capacity(request.getCapacity() != null ? request.getCapacity() : request.getCapacityAnimalUnits())
                .status(request.getStatus() != null ? request.getStatus() : "available")
                .build();

        return mapToResponse(paddockRepository.save(paddock));
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
        if (request.getCapacityAnimalUnits() != null) paddock.setCapacityAnimalUnits(request.getCapacityAnimalUnits());
        if (request.getCapacity() != null) {
            paddock.setCapacity(request.getCapacity());
            paddock.setCapacityAnimalUnits(request.getCapacity());
        }
        if (request.getStatus() != null) paddock.setStatus(request.getStatus());

        return mapToResponse(paddockRepository.save(paddock));
    }

    @Transactional
    public void delete(UUID id) {
        if (!paddockRepository.existsById(id)) {
            throw new EntityNotFoundException("Potrero no encontrado con ID: " + id);
        }
        paddockRepository.deleteById(id);
    }

    private PaddockDTO.Response mapToResponse(Paddock p) {
        return PaddockDTO.Response.builder()
                .id(p.getId())
                .farmId(p.getFarm().getId())
                .farmName(p.getFarm().getName())
                .name(p.getName())
                .code(p.getCode())
                .areaHectares(p.getAreaHectares())
                .hectares(p.getAreaHectares())
                .forageType(p.getForageType())
                .capacityAnimalUnits(p.getCapacityAnimalUnits())
                .capacity(p.getCapacity() != null ? p.getCapacity() : p.getCapacityAnimalUnits())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}