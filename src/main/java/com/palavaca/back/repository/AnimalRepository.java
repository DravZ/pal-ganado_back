package com.palavaca.back.repository;

import com.palavaca.back.model.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, UUID> {
    List<Animal> findByOrganizationId(UUID organizationId);
    List<Animal> findByFarmId(UUID farmId);
    List<Animal> findByPaddockId(UUID paddockId);
    Optional<Animal> findByOrganizationIdAndTagNumber(UUID organizationId, String tagNumber);

    @Query("SELECT a FROM Animal a WHERE a.organization.id = :orgId AND a.gender = 'M'")
    List<Animal> findSiresByOrganization(@Param("orgId") UUID orgId);

    @Query("SELECT a FROM Animal a WHERE a.organization.id = :orgId AND a.gender = 'F'")
    List<Animal> findDamsByOrganization(@Param("orgId") UUID orgId);
}