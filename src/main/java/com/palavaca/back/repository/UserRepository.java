package com.palavaca.back.repository;

import com.palavaca.back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByOrganizationId(UUID organizationId);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}