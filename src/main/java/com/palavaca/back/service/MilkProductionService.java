package com.palavaca.back.service;

import com.palavaca.back.dto.MilkProductionDTO;
import com.palavaca.back.model.Animal;
import com.palavaca.back.model.MilkProduction;
import com.palavaca.back.repository.AnimalRepository;
import com.palavaca.back.repository.MilkProductionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MilkProductionService {

    private final MilkProductionRepository milkProductionRepository;
    private final AnimalRepository animalRepository;

    @Transactional(readOnly = true)
    public List<MilkProductionDTO.Response> findByAnimalId(UUID animalId) {
        return milkProductionRepository.findByAnimalIdOrderByRecordDateDesc(animalId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MilkProductionDTO.Response findById(UUID id) {
        MilkProduction milk = milkProductionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de leche no encontrado con ID: " + id));
        return mapToResponse(milk);
    }

    @Transactional
    public MilkProductionDTO.Response create(MilkProductionDTO.CreateRequest request) {
        Animal animal = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new EntityNotFoundException("Animal no encontrado con ID: " + request.getAnimalId()));

        BigDecimal total = request.getTotalLiters();
        if (total == null) {
            BigDecimal morning = request.getMorningLiters() != null ? request.getMorningLiters() : BigDecimal.ZERO;
            BigDecimal afternoon = request.getAfternoonLiters() != null ? request.getAfternoonLiters() : BigDecimal.ZERO;
            total = morning.add(afternoon);
        }

        MilkProduction milk = MilkProduction.builder()
                .animal(animal)
                .recordDate(request.getRecordDate())
                .morningLiters(request.getMorningLiters())
                .afternoonLiters(request.getAfternoonLiters())
                .totalLiters(total)
                .notes(request.getNotes())
                .build();

        return mapToResponse(milkProductionRepository.save(milk));
    }

    @Transactional
    public MilkProductionDTO.Response update(UUID id, MilkProductionDTO.UpdateRequest request) {
        MilkProduction milk = milkProductionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de leche no encontrado con ID: " + id));

        if (request.getRecordDate() != null) milk.setRecordDate(request.getRecordDate());
        if (request.getMorningLiters() != null) milk.setMorningLiters(request.getMorningLiters());
        if (request.getAfternoonLiters() != null) milk.setAfternoonLiters(request.getAfternoonLiters());
        if (request.getTotalLiters() != null) {
            milk.setTotalLiters(request.getTotalLiters());
        } else if (request.getMorningLiters() != null || request.getAfternoonLiters() != null) {
            BigDecimal morning = milk.getMorningLiters() != null ? milk.getMorningLiters() : BigDecimal.ZERO;
            BigDecimal afternoon = milk.getAfternoonLiters() != null ? milk.getAfternoonLiters() : BigDecimal.ZERO;
            milk.setTotalLiters(morning.add(afternoon));
        }
        if (request.getNotes() != null) milk.setNotes(request.getNotes());

        return mapToResponse(milkProductionRepository.save(milk));
    }

    @Transactional
    public void delete(UUID id) {
        if (!milkProductionRepository.existsById(id)) {
            throw new EntityNotFoundException("Registro de leche no encontrado con ID: " + id);
        }
        milkProductionRepository.deleteById(id);
    }

    private MilkProductionDTO.Response mapToResponse(MilkProduction m) {
        return MilkProductionDTO.Response.builder()
                .id(m.getId())
                .animalId(m.getAnimal().getId())
                .animalTagNumber(m.getAnimal().getTagNumber())
                .recordDate(m.getRecordDate())
                .morningLiters(m.getMorningLiters())
                .afternoonLiters(m.getAfternoonLiters())
                .totalLiters(m.getTotalLiters())
                .notes(m.getNotes())
                .createdAt(m.getCreatedAt())
                .build();
    }
}