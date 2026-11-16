package com.palavaca.back.repository;

import com.palavaca.back.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("select p from Product p where p.organization.id = :organizationId and p.deletedAt is null")
    List<Product> findByOrganizationId(@Param("organizationId") UUID organizationId);
}
