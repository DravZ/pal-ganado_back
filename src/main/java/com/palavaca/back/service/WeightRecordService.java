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

        WeightRecord record = WeightRecord.builder()
                .animal(animal)
                .weighDate(request.getWeighDate())
                .weightKg(request.getWeightKg())
                .notes(request.getNotes())
                .build();

        // Actualizamos de forma automática el peso actual del animal
        animal.setCurrentWeight(request.getWeightKg());
        animalRepository.save(animal);

        return mapToResponse(weightRecordRepository.save(record));
    }

    @Transactional
    public WeightRecordDTO.Response update(UUID id, WeightRecordDTO.UpdateRequest request) {
        WeightRecord record = weightRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de peso no encontrado con ID: " + id));

        if (request.getWeighDate() != null) record.setWeighDate(request.getWeighDate());
        if (request.getWeightKg() != null) {
            record.setWeightKg(request.getWeightKg());
            // Actualizar peso actual en animal si es el pesaje más reciente
            record.getAnimal().setCurrentWeight(request.getWeightKg());
        }
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
                .notes(w.getNotes())
                .createdAt(w.getCreatedAt())
                .build();
    }
}