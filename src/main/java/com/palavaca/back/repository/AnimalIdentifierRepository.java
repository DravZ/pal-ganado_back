package com.palavaca.back.repository;

import com.palavaca.back.model.AnimalIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AnimalIdentifierRepository extends JpaRepository<AnimalIdentifier, UUID> {
    List<AnimalIdentifier> findByAnimalId(UUID animalId);

    @Query("""
            select case when count(i) > 0 then true else false end
            from AnimalIdentifier i
            where i.animal.organization.id = :organizationId
              and lower(trim(i.identifier)) = :identifier
              and (:excludeAnimalId is null or i.animal.id <> :excludeAnimalId)
            """)
    boolean existsIdentifier(
            @Param("organizationId") UUID organizationId,
            @Param("identifier") String identifier,
            @Param("excludeAnimalId") UUID excludeAnimalId);
}
