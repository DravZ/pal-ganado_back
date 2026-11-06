package com.palavaca.back.service;

import com.palavaca.back.dto.OrganizationDTO;
import com.palavaca.back.model.Organization;
import com.palavaca.back.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<OrganizationDTO.Response> findAll() {
        return organizationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizationDTO.Response findById(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + id));
        return mapToResponse(org);
    }

    @Transactional
    public OrganizationDTO.Response create(OrganizationDTO.CreateRequest request) {
        Organization org = Organization.builder()
                .name(request.getName())
                .legalName(request.getLegalName())
                .taxId(request.getTaxId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .status("active")
                .build();

        return mapToResponse(organizationRepository.save(org));
    }

    @Transactional
    public OrganizationDTO.Response update(UUID id, OrganizationDTO.UpdateRequest request) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + id));

        if (request.getName() != null) org.setName(request.getName());
        if (request.getLegalName() != null) org.setLegalName(request.getLegalName());
        if (request.getTaxId() != null) org.setTaxId(request.getTaxId());
        if (request.getPhone() != null) org.setPhone(request.getPhone());
        if (request.getEmail() != null) org.setEmail(request.getEmail());
        if (request.getStatus() != null) org.setStatus(request.getStatus());

        return mapToResponse(organizationRepository.save(org));
    }

    private OrganizationDTO.Response mapToResponse(Organization org) {
        return OrganizationDTO.Response.builder()
                .id(org.getId())
                .name(org.getName())
                .legalName(org.getLegalName())
                .taxId(org.getTaxId())
                .phone(org.getPhone())
                .email(org.getEmail())
                .status(org.getStatus())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}