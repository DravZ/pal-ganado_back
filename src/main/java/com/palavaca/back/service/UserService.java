package com.palavaca.back.service;

import com.palavaca.back.dto.UserDTO;
import com.palavaca.back.model.Organization;
import com.palavaca.back.model.User;
import com.palavaca.back.repository.OrganizationRepository;
import com.palavaca.back.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<UserDTO.Response> findByOrganizationId(UUID organizationId) {
        return userRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDTO.Response findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));
        return mapToResponse(user);
    }

    @Transactional
    public UserDTO.Response create(UserDTO.CreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el email: " + request.getEmail());
        }

        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + request.getOrganizationId()));

        User user = User.builder()
                .organization(org)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status("active")
                .build();

        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserDTO.Response update(UUID id, UserDTO.UpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("El email " + request.getEmail() + " ya está en uso");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        return mapToResponse(userRepository.save(user));
    }

    private UserDTO.Response mapToResponse(User user) {
        return UserDTO.Response.builder()
                .id(user.getId())
                .organizationId(user.getOrganization().getId())
                .organizationName(user.getOrganization().getName())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}