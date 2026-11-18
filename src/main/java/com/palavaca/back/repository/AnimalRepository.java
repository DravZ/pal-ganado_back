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
    List<Animal> findByLotIdAndStatus(UUID lotId, String status);
    Optional<Animal> findByOrganizationIdAndTagNumber(UUID organizationId, String tagNumber);

    @Query("""
            select case when count(a) > 0 then true else false end
            from Animal a
            where a.organization.id = :organizationId
              and lower(trim(a.tagNumber)) = :identifier
              and (:excludeAnimalId is null or a.id <> :excludeAnimalId)
            """)
    boolean existsIdentifier(
            @Param("organizationId") UUID organizationId,
            @Param("identifier") String identifier,
            @Param("excludeAnimalId") UUID excludeAnimalId);

    @Query("SELECT a FROM Animal a WHERE a.organization.id = :orgId AND a.gender = 'M'")
    List<Animal> findSiresByOrganization(@Param("orgId") UUID orgId);

    @Query("SELECT a FROM Animal a WHERE a.organization.id = :orgId AND a.gender = 'F'")
    List<Animal> findDamsByOrganization(@Param("orgId") UUID orgId);

    @Query("""
            select a from Animal a
            where a.acquisitionCost is not null
              and a.acquisitionCost > 0
              and not exists (
                  select 1 from Expense e
                  where e.referenceType = :referenceType
                    and e.referenceId = a.id
                    and e.deletedAt is null
              )
            """)
    List<Animal> findWithoutAcquisitionExpense(@Param("referenceType") String referenceType);
}