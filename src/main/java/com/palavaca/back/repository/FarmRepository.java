package com.palavaca.back.repository;

import com.palavaca.back.model.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FarmRepository extends JpaRepository<Farm, UUID> {

    @Query("select f from Farm f where f.organization.id = :organizationId and f.deletedAt is null")
    List<Farm> findByOrganizationId(@Param("organizationId") UUID organizationId);
}
