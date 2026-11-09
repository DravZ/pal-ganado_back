package com.palavaca.back.repository;

import com.palavaca.back.model.AnimalIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnimalIdentifierRepository extends JpaRepository<AnimalIdentifier, UUID> {
    List<AnimalIdentifier> findByAnimalId(UUID animalId);
}
