package com.palavaca.back.service;

import com.palavaca.back.dto.AnimalDTO;
import com.palavaca.back.model.Animal;
import com.palavaca.back.model.AnimalIdentifier;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.Lot;
import com.palavaca.back.model.Organization;
import com.palavaca.back.model.Paddock;
import com.palavaca.back.repository.AnimalRepository;
import com.palavaca.back.repository.AnimalIdentifierRepository;
import com.palavaca.back.repository.FarmRepository;
import com.palavaca.back.repository.OrganizationRepository;
import com.palavaca.back.repository.PaddockRepository;
import com.palavaca.back.repository.LotRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final OrganizationRepository organizationRepository;
    private final FarmRepository farmRepository;
    private final PaddockRepository paddockRepository;
    private final LotRepository lotRepository;
    private final AnimalIdentifierRepository identifierRepository;

    @Transactional(readOnly = true)
    public List<AnimalDTO.Response> findByOrganizationId(UUID organizationId) {
        return animalRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnimalDTO.Response> findByFarmId(UUID farmId) {
        return animalRepository.findByFarmId(farmId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnimalDTO.Response findById(UUID id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Animal no encontrado con ID: " + id));
        return mapToResponse(animal);
    }

    @Transactional
    public AnimalDTO.Response create(AnimalDTO.CreateRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + request.getOrganizationId()));

        Farm farm = null;
        if (request.getFarmId() != null) {
            farm = farmRepository.findById(request.getFarmId())
                    .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada con ID: " + request.getFarmId()));
        }

        Paddock paddock = null;
        if (request.getPaddockId() != null) {
            paddock = paddockRepository.findById(request.getPaddockId())
                    .orElseThrow(() -> new EntityNotFoundException("Potrero no encontrado con ID: " + request.getPaddockId()));
        }

        Lot lot = null;
        if (request.getLotId() != null) {
            lot = lotRepository.findById(request.getLotId())
                    .orElseThrow(() -> new EntityNotFoundException("Lote no encontrado con ID: " + request.getLotId()));
        }

        Animal sire = null;
        if (request.getSireId() != null) {
            sire = animalRepository.findById(request.getSireId())
                    .orElseThrow(() -> new EntityNotFoundException("Padre (toro) no encontrado con ID: " + request.getSireId()));
        }

        Animal dam = null;
        if (request.getDamId() != null) {
            dam = animalRepository.findById(request.getDamId())
                    .orElseThrow(() -> new EntityNotFoundException("Madre (vaca) no encontrada con ID: " + request.getDamId()));
        }

        Animal animal = Animal.builder()
                .organization(org)
                .farm(farm)
                .paddock(paddock)
                .lot(lot)
                .tagNumber(request.getTagNumber() != null ? request.getTagNumber() : request.getIdentifierCode())
                .name(request.getName())
                .gender(request.getGender())
                .species(request.getSpecies())
                .birthDate(request.getBirthDate())
                .breed(request.getBreed())
                .category(request.getCategory())
                .purpose(request.getPurpose())
                .weightAtBirth(request.getWeightAtBirth())
                .currentWeight(request.getCurrentWeight())
                .currentWeightDate(request.getCurrentWeightDate())
                .acquisitionDate(request.getAcquisitionDate())
                .acquisitionCost(request.getAcquisitionCost())
                .notes(request.getNotes())
                .status("active")
                .sire(sire)
                .dam(dam)
                .build();

        Animal savedAnimal = animalRepository.save(animal);
        if (request.getIdentifierCode() != null && !request.getIdentifierCode().isBlank()) {
            identifierRepository.save(AnimalIdentifier.builder()
                    .animal(savedAnimal)
                    .identifierType(request.getIdentifierType() != null ? request.getIdentifierType() : "ARETE_VISUAL")
                    .identifier(request.getIdentifierCode())
                    .official(Boolean.TRUE.equals(request.getIsOfficial()))
                    .current(request.getIsCurrent() == null || request.getIsCurrent())
                    .build());
        } else if (request.getTagNumber() == null || request.getTagNumber().isBlank()) {
            throw new IllegalArgumentException("El arete o identificador es obligatorio");
        }
        return mapToResponse(savedAnimal);
    }

    @Transactional
    public AnimalDTO.Response update(UUID id, AnimalDTO.UpdateRequest request) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Animal no encontrado con ID: " + id));

        if (request.getFarmId() != null) {
            Farm farm = farmRepository.findById(request.getFarmId())
                    .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada"));
            animal.setFarm(farm);
        }

        if (request.getPaddockId() != null) {
            Paddock paddock = paddockRepository.findById(request.getPaddockId())
                    .orElseThrow(() -> new EntityNotFoundException("Potrero no encontrado"));
            animal.setPaddock(paddock);
        }

        if (request.getLotId() != null) {
            Lot lot = lotRepository.findById(request.getLotId())
                    .orElseThrow(() -> new EntityNotFoundException("Lote no encontrado"));
            animal.setLot(lot);
        }

        if (request.getTagNumber() != null) animal.setTagNumber(request.getTagNumber());
        if (request.getName() != null) animal.setName(request.getName());
        if (request.getGender() != null) animal.setGender(request.getGender());
        if (request.getSpecies() != null) animal.setSpecies(request.getSpecies());
        if (request.getBirthDate() != null) animal.setBirthDate(request.getBirthDate());
        if (request.getBreed() != null) animal.setBreed(request.getBreed());
        if (request.getCategory() != null) animal.setCategory(request.getCategory());
        if (request.getPurpose() != null) animal.setPurpose(request.getPurpose());
        if (request.getCurrentWeight() != null) animal.setCurrentWeight(request.getCurrentWeight());
        if (request.getCurrentWeightDate() != null) animal.setCurrentWeightDate(request.getCurrentWeightDate());
        if (request.getAcquisitionDate() != null) animal.setAcquisitionDate(request.getAcquisitionDate());
        if (request.getAcquisitionCost() != null) animal.setAcquisitionCost(request.getAcquisitionCost());
        if (request.getNotes() != null) animal.setNotes(request.getNotes());
        if (request.getStatus() != null) animal.setStatus(request.getStatus());

        if (request.getSireId() != null) {
            Animal sire = animalRepository.findById(request.getSireId())
                    .orElseThrow(() -> new EntityNotFoundException("Padre no encontrado"));
            animal.setSire(sire);
        }

        if (request.getDamId() != null) {
            Animal dam = animalRepository.findById(request.getDamId())
                    .orElseThrow(() -> new EntityNotFoundException("Madre no encontrada"));
            animal.setDam(dam);
        }

        return mapToResponse(animalRepository.save(animal));
    }

    @Transactional
    public void delete(UUID id) {
        if (!animalRepository.existsById(id)) {
            throw new EntityNotFoundException("Animal no encontrado con ID: " + id);
        }
        animalRepository.deleteById(id);
    }

    private AnimalDTO.Response mapToResponse(Animal a) {
        return AnimalDTO.Response.builder()
                .id(a.getId())
                .organizationId(a.getOrganization().getId())
                .farmId(a.getFarm() != null ? a.getFarm().getId() : null)
                .farmName(a.getFarm() != null ? a.getFarm().getName() : null)
                .paddockId(a.getPaddock() != null ? a.getPaddock().getId() : null)
                .paddockName(a.getPaddock() != null ? a.getPaddock().getName() : null)
                .lotId(a.getLot() != null ? a.getLot().getId() : null)
                .lotName(a.getLot() != null ? a.getLot().getName() : null)
                .tagNumber(a.getTagNumber())
                .name(a.getName())
                .gender(a.getGender())
                .sex(a.getGender())
                .identifierTag(a.getTagNumber())
                .species(a.getSpecies())
                .birthDate(a.getBirthDate())
                .breed(a.getBreed())
                .category(a.getCategory())
                .purpose(a.getPurpose())
                .weightAtBirth(a.getWeightAtBirth())
                .birthWeight(a.getWeightAtBirth())
                .currentWeight(a.getCurrentWeight())
                .currentWeightDate(a.getCurrentWeightDate())
                .acquisitionDate(a.getAcquisitionDate())
                .acquisitionCost(a.getAcquisitionCost())
                .notes(a.getNotes())
                .status(a.getStatus())
                .sireId(a.getSire() != null ? a.getSire().getId() : null)
                .sireTagNumber(a.getSire() != null ? a.getSire().getTagNumber() : null)
                .damId(a.getDam() != null ? a.getDam().getId() : null)
                .damTagNumber(a.getDam() != null ? a.getDam().getTagNumber() : null)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}