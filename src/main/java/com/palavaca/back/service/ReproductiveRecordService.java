package com.palavaca.back.service;

import com.palavaca.back.dto.ReproductiveRecordDTO;
import com.palavaca.back.model.Animal;
import com.palavaca.back.model.ReproductiveRecord;
import com.palavaca.back.repository.AnimalRepository;
import com.palavaca.back.repository.ReproductiveRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReproductiveRecordService {

    private final ReproductiveRecordRepository reproductiveRecordRepository;
    private final AnimalRepository animalRepository;

    @Transactional(readOnly = true)
    public List<ReproductiveRecordDTO.Response> findByAnimalId(UUID animalId) {
        return reproductiveRecordRepository.findByAnimalIdOrderByEventDateDesc(animalId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReproductiveRecordDTO.Response findById(UUID id) {
        ReproductiveRecord record = reproductiveRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro reproductivo no encontrado con ID: " + id));
        return mapToResponse(record);
    }

    @Transactional
    public ReproductiveRecordDTO.Response create(ReproductiveRecordDTO.CreateRequest request) {
        UUID femaleId = request.getAnimalId() != null ? request.getAnimalId() : request.getFemaleAnimalId();
        if (femaleId == null) throw new IllegalArgumentException("El animal hembra es obligatorio");
        Animal animal = animalRepository.findById(femaleId)
                .orElseThrow(() -> new EntityNotFoundException("Animal no encontrado con ID: " + femaleId));

        Animal sire = null;
        UUID maleId = request.getSireId() != null ? request.getSireId() : request.getMaleAnimalId();
        if (maleId != null) {
            sire = animalRepository.findById(maleId)
                    .orElseThrow(() -> new EntityNotFoundException("Toro no encontrado con ID: " + maleId));
        }

        // Auto-calcular fecha estimada de parto (~283 días para bovinos si es inseminación/monta)
        LocalDate expectedBirth = request.getExpectedBirthDate() != null
                ? request.getExpectedBirthDate() : request.getExpectedDate();
        if (expectedBirth == null && "Insemination".equalsIgnoreCase(request.getEventType()) && request.getEventDate() != null) {
            expectedBirth = request.getEventDate().plusDays(283);
        }

        ReproductiveRecord record = ReproductiveRecord.builder()
                .animal(animal)
                .eventDate(request.getEventDate())
                .eventType(request.getEventType())
                .sire(sire)
                .expectedBirthDate(expectedBirth)
                .pregnancyStatus(request.getPregnancyStatus())
                .notes(request.getNotes())
                .build();

        return mapToResponse(reproductiveRecordRepository.save(record));
    }

    @Transactional
    public ReproductiveRecordDTO.Response update(UUID id, ReproductiveRecordDTO.UpdateRequest request) {
        ReproductiveRecord record = reproductiveRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro reproductivo no encontrado con ID: " + id));

        if (request.getSireId() != null) {
            Animal sire = animalRepository.findById(request.getSireId())
                    .orElseThrow(() -> new EntityNotFoundException("Toro no encontrado"));
            record.setSire(sire);
        }

        if (request.getEventDate() != null) record.setEventDate(request.getEventDate());
        if (request.getEventType() != null) record.setEventType(request.getEventType());
        if (request.getExpectedBirthDate() != null) record.setExpectedBirthDate(request.getExpectedBirthDate());
        if (request.getExpectedDate() != null) record.setExpectedBirthDate(request.getExpectedDate());
        if (request.getPregnancyStatus() != null) record.setPregnancyStatus(request.getPregnancyStatus());
        if (request.getNotes() != null) record.setNotes(request.getNotes());

        return mapToResponse(reproductiveRecordRepository.save(record));
    }

    @Transactional
    public void delete(UUID id) {
        if (!reproductiveRecordRepository.existsById(id)) {
            throw new EntityNotFoundException("Registro reproductivo no encontrado con ID: " + id);
        }
        reproductiveRecordRepository.deleteById(id);
    }

    private ReproductiveRecordDTO.Response mapToResponse(ReproductiveRecord r) {
        return ReproductiveRecordDTO.Response.builder()
                .id(r.getId())
                .animalId(r.getAnimal().getId())
                .femaleAnimalId(r.getAnimal().getId())
                .animalTagNumber(r.getAnimal().getTagNumber())
                .eventDate(r.getEventDate())
                .eventType(r.getEventType())
                .sireId(r.getSire() != null ? r.getSire().getId() : null)
                .maleAnimalId(r.getSire() != null ? r.getSire().getId() : null)
                .sireTagNumber(r.getSire() != null ? r.getSire().getTagNumber() : null)
                .expectedBirthDate(r.getExpectedBirthDate())
                .expectedDate(r.getExpectedBirthDate())
                .pregnancyStatus(r.getPregnancyStatus())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .build();
    }
}