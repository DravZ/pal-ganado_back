package com.palavaca.back.service;

import com.palavaca.back.dto.WeightRecordDTO;
import com.palavaca.back.model.Animal;
import com.palavaca.back.model.WeightRecord;
import com.palavaca.back.repository.AnimalRepository;
import com.palavaca.back.repository.WeightRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WeightRecordService {

    private final WeightRecordRepository weightRecordRepository;
    private final AnimalRepository animalRepository;

    @Transactional(readOnly = true)
    public List<WeightRecordDTO.Response> findByAnimalId(UUID animalId) {
        return weightRecordRepository.findByAnimalIdOrderByWeighDateDesc(animalId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WeightRecordDTO.Response findById(UUID id) {
        WeightRecord record = weightRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de peso no encontrado con ID: " + id));
        return mapToResponse(record);
    }

    @Transactional
    public WeightRecordDTO.Response create(WeightRecordDTO.CreateRequest request) {
        Animal animal = animalRepository.findById(request.getAnimalId())
                .orElseThrow(() -> new EntityNotFoundException("Animal no encontrado con ID: " + request.getAnimalId()));

        LocalDate measuredDate = request.getWeighDate() != null ? request.getWeighDate()
                : request.getMeasuredAt() != null ? request.getMeasuredAt().toLocalDate()
                : request.getDate() != null ? request.getDate().toLocalDate() : null;
        if (measuredDate == null) throw new IllegalArgumentException("La fecha del pesaje es obligatoria");
        WeightRecord record = WeightRecord.builder()
                .animal(animal)
                .weighDate(measuredDate)
                .weightKg(request.getWeightKg())
                .measuredAt(request.getMeasuredAt() != null ? request.getMeasuredAt()
                        : request.getDate() != null ? request.getDate() : measuredDate.atStartOfDay())
                .method(request.getMethod())
                .notes(request.getNotes())
                .build();

        // Actualizamos de forma automática el peso actual del animal
        animal.setCurrentWeight(request.getWeightKg());
        animal.setCurrentWeightDate(measuredDate);
        animalRepository.save(animal);

        return mapToResponse(weightRecordRepository.save(record));
    }

    @Transactional
    public WeightRecordDTO.Response update(UUID id, WeightRecordDTO.UpdateRequest request) {
        WeightRecord record = weightRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de peso no encontrado con ID: " + id));

        if (request.getWeighDate() != null) record.setWeighDate(request.getWeighDate());
        if (request.getMeasuredAt() != null) {
            record.setMeasuredAt(request.getMeasuredAt());
            record.setWeighDate(request.getMeasuredAt().toLocalDate());
        }
        if (request.getWeightKg() != null) {
            record.setWeightKg(request.getWeightKg());
            // Actualizar peso actual en animal si es el pesaje más reciente
            record.getAnimal().setCurrentWeight(request.getWeightKg());
        }
        if (request.getMethod() != null) record.setMethod(request.getMethod());
        if (request.getNotes() != null) record.setNotes(request.getNotes());

        return mapToResponse(weightRecordRepository.save(record));
    }

    @Transactional
    public void delete(UUID id) {
        if (!weightRecordRepository.existsById(id)) {
            throw new EntityNotFoundException("Registro de peso no encontrado con ID: " + id);
        }
        weightRecordRepository.deleteById(id);
    }

    private WeightRecordDTO.Response mapToResponse(WeightRecord w) {
        return WeightRecordDTO.Response.builder()
                .id(w.getId())
                .animalId(w.getAnimal().getId())
                .animalTagNumber(w.getAnimal().getTagNumber())
                .weighDate(w.getWeighDate())
                .weightKg(w.getWeightKg())
                .weight(w.getWeightKg())
                .measuredAt(w.getMeasuredAt() != null ? w.getMeasuredAt() : w.getWeighDate().atStartOfDay())
                .method(w.getMethod())
                .notes(w.getNotes())
                .createdAt(w.getCreatedAt())
                .build();
    }
}