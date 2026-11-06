package com.palavaca.back.service;

import com.palavaca.back.dto.MedicationDTO;
import com.palavaca.back.model.Medication;
import com.palavaca.back.repository.MedicationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;

    @Transactional(readOnly = true)
    public List<MedicationDTO.Response> findAll() {
        return medicationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicationDTO.Response findById(UUID id) {
        Medication med = medicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicamento no encontrado con ID: " + id));
        return mapToResponse(med);
    }

    @Transactional
    public MedicationDTO.Response create(MedicationDTO.CreateRequest request) {
        Medication med = Medication.builder()
                .name(request.getName())
                .activeIngredient(request.getActiveIngredient())
                .category(request.getCategory())
                .presentation(request.getPresentation())
                .defaultUnit(request.getDefaultUnit())
                .withdrawalDaysMilk(request.getWithdrawalDaysMilk())
                .withdrawalDaysMeat(request.getWithdrawalDaysMeat())
                .defaultDosage(request.getDefaultDosage())
                .build();

        return mapToResponse(medicationRepository.save(med));
    }

    @Transactional
    public MedicationDTO.Response update(UUID id, MedicationDTO.UpdateRequest request) {
        Medication med = medicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicamento no encontrado con ID: " + id));

        if (request.getName() != null) med.setName(request.getName());
        if (request.getActiveIngredient() != null) med.setActiveIngredient(request.getActiveIngredient());
        if (request.getCategory() != null) med.setCategory(request.getCategory());
        if (request.getPresentation() != null) med.setPresentation(request.getPresentation());
        if (request.getDefaultUnit() != null) med.setDefaultUnit(request.getDefaultUnit());
        if (request.getWithdrawalDaysMilk() != null) med.setWithdrawalDaysMilk(request.getWithdrawalDaysMilk());
        if (request.getWithdrawalDaysMeat() != null) med.setWithdrawalDaysMeat(request.getWithdrawalDaysMeat());
        if (request.getDefaultDosage() != null) med.setDefaultDosage(request.getDefaultDosage());

        return mapToResponse(medicationRepository.save(med));
    }

    @Transactional
    public void delete(UUID id) {
        if (!medicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Medicamento no encontrado con ID: " + id);
        }
        medicationRepository.deleteById(id);
    }

    private MedicationDTO.Response mapToResponse(Medication med) {
        return MedicationDTO.Response.builder()
                .id(med.getId())
                .name(med.getName())
                .activeIngredient(med.getActiveIngredient())
                .category(med.getCategory())
                .presentation(med.getPresentation())
                .defaultUnit(med.getDefaultUnit())
                .withdrawalDaysMilk(med.getWithdrawalDaysMilk())
                .withdrawalDaysMeat(med.getWithdrawalDaysMeat())
                .defaultDosage(med.getDefaultDosage())
                .createdAt(med.getCreatedAt())
                .build();
    }
}