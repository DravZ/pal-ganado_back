package com.palavaca.back.service;

import com.palavaca.back.dto.SupplierDTO;
import com.palavaca.back.model.Organization;
import com.palavaca.back.model.Supplier;
import com.palavaca.back.repository.OrganizationRepository;
import com.palavaca.back.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<SupplierDTO.Response> findByOrganizationId(UUID organizationId) {
        return supplierRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierDTO.Response findById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado con ID: " + id));
        return mapToResponse(supplier);
    }

    @Transactional
    public SupplierDTO.Response create(SupplierDTO.CreateRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + request.getOrganizationId()));

        Supplier supplier = Supplier.builder()
                .organization(org)
                .name(request.getName())
                .contactName(request.getContactName())
                .taxId(request.getTaxId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .build();

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierDTO.Response update(UUID id, SupplierDTO.UpdateRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado con ID: " + id));

        if (request.getName() != null) supplier.setName(request.getName());
        if (request.getContactName() != null) supplier.setContactName(request.getContactName());
        if (request.getTaxId() != null) supplier.setTaxId(request.getTaxId());
        if (request.getPhone() != null) supplier.setPhone(request.getPhone());
        if (request.getEmail() != null) supplier.setEmail(request.getEmail());
        if (request.getAddress() != null) supplier.setAddress(request.getAddress());

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void delete(UUID id) {
        if (!supplierRepository.existsById(id)) {
            throw new EntityNotFoundException("Proveedor no encontrado con ID: " + id);
        }
        supplierRepository.deleteById(id);
    }

    private SupplierDTO.Response mapToResponse(Supplier s) {
        return SupplierDTO.Response.builder()
                .id(s.getId())
                .organizationId(s.getOrganization().getId())
                .name(s.getName())
                .contactName(s.getContactName())
                .taxId(s.getTaxId())
                .phone(s.getPhone())
                .email(s.getEmail())
                .address(s.getAddress())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}