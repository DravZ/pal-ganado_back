package com.palavaca.back.service;

import com.palavaca.back.dto.CustomerDTO;
import com.palavaca.back.model.Customer;
import com.palavaca.back.model.Organization;
import com.palavaca.back.repository.CustomerRepository;
import com.palavaca.back.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<CustomerDTO.Response> findByOrganizationId(UUID organizationId) {
        return customerRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerDTO.Response findById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));
        return mapToResponse(customer);
    }

    @Transactional
    public CustomerDTO.Response create(CustomerDTO.CreateRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + request.getOrganizationId()));

        Customer customer = Customer.builder()
                .organization(org)
                .name(request.getName())
                .taxId(request.getTaxId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .build();

        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerDTO.Response update(UUID id, CustomerDTO.UpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + id));

        if (request.getName() != null) customer.setName(request.getName());
        if (request.getTaxId() != null) customer.setTaxId(request.getTaxId());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());

        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Cliente no encontrado con ID: " + id);
        }
        customerRepository.deleteById(id);
    }

    private CustomerDTO.Response mapToResponse(Customer c) {
        return CustomerDTO.Response.builder()
                .id(c.getId())
                .organizationId(c.getOrganization().getId())
                .name(c.getName())
                .taxId(c.getTaxId())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}