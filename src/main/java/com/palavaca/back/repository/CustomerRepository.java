package com.palavaca.back.repository;

import com.palavaca.back.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query("select c from Customer c where c.organization.id = :organizationId and c.deletedAt is null")
    List<Customer> findByOrganizationId(@Param("organizationId") UUID organizationId);
}
