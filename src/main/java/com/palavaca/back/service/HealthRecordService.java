package com.palavaca.back.service;

import com.palavaca.back.dto.HealthRecordDTO;
import com.palavaca.back.model.Animal;
import com.palavaca.back.model.HealthRecord;
import com.palavaca.back.model.Medication;
import com.palavaca.back.repository.AnimalRepository;
import com.palavaca.back.repository.HealthRecordRepository;
import com.palavaca.back.repository.MedicationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final AnimalRepository animalRepository;
    private final MedicationRepository medicationRepository;

    @Transactional(readOnly = true)
    public List<HealthRecordDTO.Response> findByAnimalId(UUID animalId) {
        return healthRecordRepository.findByAnimalIdOrderByEventDateDesc(animalId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HealthRecordDTO.Response findById(UUID id) {
        HealthRecord record = healthRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de salud no encontrado con ID: " + id));
        return mapToResponse(record);
    }

    @Transactional
    public HealthRecordDTO.Response create(HealthRecordDTO.CreateRequest request) {
        Animal animal = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new EntityNotFoundException("Animal no encontrado con ID: " + request.getAnimalId()));

        Medication medication = null;
        if (request.getMedicationId() != null) {
            medication = medicationRepository.findById(request.getMedicationId())
                    .orElseThrow(() -> new EntityNotFoundException("Medicamento no encontrado con ID: " + request.getMedicationId()));
        }

        HealthRecord record = HealthRecord.builder()
                .animal(animal)
                .medication(medication)
                .eventDate(request.getEventDate())
                .eventType(request.getEventType())
                .diagnosis(request.getDiagnosis())
                .symptoms(request.getSymptoms())
                .result(request.getResult())
                .veterinarianName(request.getVeterinarianName())
                .dosage(request.getDosage())
                .unit(request.getUnit())
                .withdrawalEndDate(request.getWithdrawalEndDate())
                .notes(request.getNotes())
                .build();

        return mapToResponse(healthRecordRepository.save(record));
    }

    @Transactional
    public HealthRecordDTO.Response update(UUID id, HealthRecordDTO.UpdateRequest request) {
        HealthRecord record = healthRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de salud no encontrado con ID: " + id));

        if (request.getMedicationId() != null) {
            Medication medication = medicationRepository.findById(request.getMedicationId())
                    .orElseThrow(() -> new EntityNotFoundException("Medicamento no encontrado"));
            record.setMedication(medication);
        }

        if (request.getEventDate() != null) record.setEventDate(request.getEventDate());
        if (request.getEventType() != null) record.setEventType(request.getEventType());
        if (request.getDiagnosis() != null) record.setDiagnosis(request.getDiagnosis());
        if (request.getSymptoms() != null) record.setSymptoms(request.getSymptoms());
        if (request.getResult() != null) record.setResult(request.getResult());
        if (request.getVeterinarianName() != null) record.setVeterinarianName(request.getVeterinarianName());
        if (request.getDosage() != null) record.setDosage(request.getDosage());
        if (request.getUnit() != null) record.setUnit(request.getUnit());
        if (request.getWithdrawalEndDate() != null) record.setWithdrawalEndDate(request.getWithdrawalEndDate());
        if (request.getNotes() != null) record.setNotes(request.getNotes());

        return mapToResponse(healthRecordRepository.save(record));
    }

    @Transactional
    public void delete(UUID id) {
        if (!healthRecordRepository.existsById(id)) {
            throw new EntityNotFoundException("Registro de salud no encontrado con ID: " + id);
        }
        healthRecordRepository.deleteById(id);
    }

    private HealthRecordDTO.Response mapToResponse(HealthRecord h) {
        return HealthRecordDTO.Response.builder()
                .id(h.getId())
                .animalId(h.getAnimal().getId())
                .animalTagNumber(h.getAnimal().getTagNumber())
                .medicationId(h.getMedication() != null ? h.getMedication().getId() : null)
                .medicationName(h.getMedication() != null ? h.getMedication().getName() : null)
                .eventDate(h.getEventDate())
                .eventType(h.getEventType())
                .diagnosis(h.getDiagnosis())
                .symptoms(h.getSymptoms())
                .result(h.getResult())
                .veterinarianName(h.getVeterinarianName())
                .dosage(h.getDosage())
                .unit(h.getUnit())
                .withdrawalEndDate(h.getWithdrawalEndDate())
                .notes(h.getNotes())
                .createdAt(h.getCreatedAt())
                .build();
    }
}