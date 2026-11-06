package com.palavaca.back.service;

import com.palavaca.back.dto.SalesRecordDTO;
import com.palavaca.back.model.*;
import com.palavaca.back.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalesRecordService {

    private final SalesRecordRepository salesRecordRepository;
    private final OrganizationRepository organizationRepository;
    private final CustomerRepository customerRepository;
    private final AnimalRepository animalRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<SalesRecordDTO.Response> findByOrganizationId(UUID organizationId) {
        return salesRecordRepository.findByOrganizationIdOrderBySaleDateDesc(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesRecordDTO.Response findById(UUID id) {
        SalesRecord record = salesRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con ID: " + id));
        return mapToResponse(record);
    }

    @Transactional
    public SalesRecordDTO.Response create(SalesRecordDTO.CreateRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + request.getOrganizationId()));

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + request.getCustomerId()));
        }

        Animal animal = null;
        if (request.getAnimalId() != null) {
            animal = animalRepository.findById(request.getAnimalId())
                    .orElseThrow(() -> new EntityNotFoundException("Animal no encontrado con ID: " + request.getAnimalId()));
            // Si el animal es vendido, actualizamos su estado
            animal.setStatus("sold");
            animalRepository.save(animal);
        }

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + request.getProductId()));
        }

        BigDecimal total = request.getTotalAmount();
        if (total == null && request.getQuantity() != null && request.getUnitPrice() != null) {
            total = request.getQuantity().multiply(request.getUnitPrice());
        }

        SalesRecord record = SalesRecord.builder()
                .organization(org)
                .customer(customer)
                .animal(animal)
                .product(product)
                .saleDate(request.getSaleDate())
                .concept(request.getConcept())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalAmount(total)
                .paymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "Paid")
                .notes(request.getNotes())
                .build();

        return mapToResponse(salesRecordRepository.save(record));
    }

    @Transactional
    public SalesRecordDTO.Response update(UUID id, SalesRecordDTO.UpdateRequest request) {
        SalesRecord record = salesRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con ID: " + id));

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
            record.setCustomer(customer);
        }

        if (request.getSaleDate() != null) record.setSaleDate(request.getSaleDate());
        if (request.getConcept() != null) record.setConcept(request.getConcept());
        if (request.getQuantity() != null) record.setQuantity(request.getQuantity());
        if (request.getUnitPrice() != null) record.setUnitPrice(request.getUnitPrice());
        if (request.getTotalAmount() != null) record.setTotalAmount(request.getTotalAmount());
        if (request.getPaymentStatus() != null) record.setPaymentStatus(request.getPaymentStatus());
        if (request.getNotes() != null) record.setNotes(request.getNotes());

        return mapToResponse(salesRecordRepository.save(record));
    }

    @Transactional
    public void delete(UUID id) {
        if (!salesRecordRepository.existsById(id)) {
            throw new EntityNotFoundException("Venta no encontrada con ID: " + id);
        }
        salesRecordRepository.deleteById(id);
    }

    private SalesRecordDTO.Response mapToResponse(SalesRecord s) {
        return SalesRecordDTO.Response.builder()
                .id(s.getId())
                .organizationId(s.getOrganization().getId())
                .customerId(s.getCustomer() != null ? s.getCustomer().getId() : null)
                .customerName(s.getCustomer() != null ? s.getCustomer().getName() : null)
                .animalId(s.getAnimal() != null ? s.getAnimal().getId() : null)
                .animalTagNumber(s.getAnimal() != null ? s.getAnimal().getTagNumber() : null)
                .productId(s.getProduct() != null ? s.getProduct().getId() : null)
                .productName(s.getProduct() != null ? s.getProduct().getName() : null)
                .saleDate(s.getSaleDate())
                .concept(s.getConcept())
                .quantity(s.getQuantity())
                .unitPrice(s.getUnitPrice())
                .totalAmount(s.getTotalAmount())
                .paymentStatus(s.getPaymentStatus())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .build();
    }
}