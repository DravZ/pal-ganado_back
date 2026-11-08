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
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SalesRecordService {

    private final SalesRecordRepository salesRecordRepository;
    private final OrganizationRepository organizationRepository;
    private final CustomerRepository customerRepository;
    private final AnimalRepository animalRepository;
    private final ProductRepository productRepository;
    private final FarmRepository farmRepository;
    private final SalesLineRepository salesLineRepository;
    private final LotRepository lotRepository;

    @Transactional(readOnly = true)
    public List<SalesRecordDTO.Response> findByOrganizationId(UUID organizationId) {
        return salesRecordRepository.findByOrganizationIdOrderBySaleDateDesc(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SalesRecordDTO.Response> findByFarmId(UUID farmId) {
        return salesRecordRepository.findByFarmIdOrderBySaleDateDesc(farmId).stream()
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
        Farm farm = request.getFarmId() == null ? null : farmRepository.findById(request.getFarmId())
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada con ID: " + request.getFarmId()));
        UUID organizationId = request.getOrganizationId() != null
                ? request.getOrganizationId() : farm == null ? null : farm.getOrganization().getId();
        if (organizationId == null) throw new IllegalArgumentException("La organización o finca es obligatoria");
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + organizationId));

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

        BigDecimal subtotal = request.getSubtotal();
        BigDecimal total = request.getTotalAmount() != null ? request.getTotalAmount() : request.getTotal();
        if (subtotal == null && request.getLines() != null) {
            subtotal = request.getLines().stream()
                    .map(line -> line.getSubtotal() != null ? line.getSubtotal()
                            : line.getWeight() != null && line.getUnitPrice() != null
                            ? line.getWeight().multiply(line.getUnitPrice())
                            : line.getQuantity() != null && line.getUnitPrice() != null
                            ? line.getQuantity().multiply(line.getUnitPrice()) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (total == null && request.getQuantity() != null && request.getUnitPrice() != null) {
            total = request.getQuantity().multiply(request.getUnitPrice());
        }
        if (total == null && subtotal != null) total = subtotal.add(request.getTax() == null ? BigDecimal.ZERO : request.getTax());
        if (total == null) throw new IllegalArgumentException("El monto total de la venta es obligatorio");

        SalesRecord record = SalesRecord.builder()
                .organization(org)
                .farm(farm)
                .saleNumber(request.getSaleNumber())
                .customer(customer)
                .animal(animal)
                .product(product)
                .saleDate(request.getSaleDate())
                .concept(request.getConcept())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .subtotal(subtotal)
                .tax(request.getTax())
                .totalAmount(total)
                .paymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "Paid")
                .notes(request.getNotes())
                .build();

        SalesRecord saved = salesRecordRepository.save(record);
        if (request.getLines() != null) {
            for (SalesRecordDTO.LineRequest line : request.getLines()) {
                Animal lineAnimal = line.getAnimalId() == null ? null : animalRepository.findById(line.getAnimalId())
                        .orElseThrow(() -> new EntityNotFoundException("Animal no encontrado"));
                Lot lineLot = line.getLotId() == null ? null : lotRepository.findById(line.getLotId())
                        .orElseThrow(() -> new EntityNotFoundException("Lote no encontrado"));
                Product lineProduct = line.getProductId() == null ? null : productRepository.findById(line.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
                if (lineAnimal != null) {
                    lineAnimal.setStatus("sold");
                    animalRepository.save(lineAnimal);
                }
                salesLineRepository.save(SalesLine.builder()
                        .sale(saved)
                        .animal(lineAnimal)
                        .lot(lineLot)
                        .product(lineProduct)
                        .quantity(line.getQuantity())
                        .weight(line.getWeight())
                        .unitPrice(line.getUnitPrice())
                        .subtotal(line.getSubtotal())
                        .build());
            }
        }
        return mapToResponse(saved);
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
        if (request.getSubtotal() != null) record.setSubtotal(request.getSubtotal());
        if (request.getTax() != null) record.setTax(request.getTax());
        if (request.getTotalAmount() != null) record.setTotalAmount(request.getTotalAmount());
        if (request.getTotal() != null) record.setTotalAmount(request.getTotal());
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
                .farmId(s.getFarm() != null ? s.getFarm().getId() : null)
                .saleNumber(s.getSaleNumber())
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
                .subtotal(s.getSubtotal())
                .tax(s.getTax())
                .total(s.getTotalAmount())
                .totalAmount(s.getTotalAmount())
                .paymentStatus(s.getPaymentStatus())
                .notes(s.getNotes())
                .lines(salesLineRepository.findBySaleId(s.getId()).stream()
                        .map(line -> SalesRecordDTO.LineResponse.builder()
                                .id(line.getId())
                                .itemType(line.getAnimal() != null ? "ANIMAL" : line.getLot() != null ? "LOT" : "PRODUCT")
                                .animalId(line.getAnimal() != null ? line.getAnimal().getId() : null)
                                .lotId(line.getLot() != null ? line.getLot().getId() : null)
                                .productId(line.getProduct() != null ? line.getProduct().getId() : null)
                                .quantity(line.getQuantity())
                                .weight(line.getWeight())
                                .unitPrice(line.getUnitPrice())
                                .subtotal(line.getSubtotal())
                                .build())
                        .toList())
                .createdAt(s.getCreatedAt())
                .build();
    }
}