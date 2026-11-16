package com.palavaca.back.repository;

import com.palavaca.back.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    @Query("select s from Supplier s where s.organization.id = :organizationId and s.deletedAt is null")
    List<Supplier> findByOrganizationId(@Param("organizationId") UUID organizationId);
}
