package com.palavaca.back.service;

import com.palavaca.back.dto.FarmDTO;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.Organization;
import com.palavaca.back.repository.FarmRepository;
import com.palavaca.back.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FarmService {

    private final FarmRepository farmRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<FarmDTO.Response> findByOrganizationId(UUID organizationId) {
        return farmRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FarmDTO.Response findById(UUID id) {
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada con ID: " + id));
        return mapToResponse(farm);
    }

    @Transactional
    public FarmDTO.Response create(FarmDTO.CreateRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + request.getOrganizationId()));

        Farm farm = Farm.builder()
                .organization(org)
                .name(request.getName())
                .location(request.getLocation())
                .totalAreaHectares(request.getTotalAreaHectares())
                .build();

        return mapToResponse(farmRepository.save(farm));
    }

    @Transactional
    public FarmDTO.Response update(UUID id, FarmDTO.UpdateRequest request) {
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada con ID: " + id));

        if (request.getName() != null) farm.setName(request.getName());
        if (request.getLocation() != null) farm.setLocation(request.getLocation());
        if (request.getTotalAreaHectares() != null) farm.setTotalAreaHectares(request.getTotalAreaHectares());

        return mapToResponse(farmRepository.save(farm));
    }

    @Transactional
    public void delete(UUID id) {
        if (!farmRepository.existsById(id)) {
            throw new EntityNotFoundException("Finca no encontrada con ID: " + id);
        }
        farmRepository.deleteById(id);
    }

    private FarmDTO.Response mapToResponse(Farm f) {
        return FarmDTO.Response.builder()
                .id(f.getId())
                .organizationId(f.getOrganization().getId())
                .name(f.getName())
                .location(f.getLocation())
                .totalAreaHectares(f.getTotalAreaHectares())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}