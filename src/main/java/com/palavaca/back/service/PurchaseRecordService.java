package com.palavaca.back.service;

import com.palavaca.back.dto.PurchaseRecordDTO;
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
public class PurchaseRecordService {

    private final PurchaseRecordRepository purchaseRecordRepository;
    private final OrganizationRepository organizationRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final MedicationRepository medicationRepository;

    @Transactional(readOnly = true)
    public List<PurchaseRecordDTO.Response> findByOrganizationId(UUID organizationId) {
        return purchaseRecordRepository.findByOrganizationIdOrderByPurchaseDateDesc(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseRecordDTO.Response findById(UUID id) {
        PurchaseRecord record = purchaseRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compra no encontrada con ID: " + id));
        return mapToResponse(record);
    }

    @Transactional
    public PurchaseRecordDTO.Response create(PurchaseRecordDTO.CreateRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + request.getOrganizationId()));

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado con ID: " + request.getSupplierId()));
        }

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        }

        Medication medication = null;
        if (request.getMedicationId() != null) {
            medication = medicationRepository.findById(request.getMedicationId())
                    .orElseThrow(() -> new EntityNotFoundException("Medicamento no encontrado"));
        }

        BigDecimal total = request.getTotalAmount();
        if (total == null && request.getQuantity() != null && request.getUnitPrice() != null) {
            total = request.getQuantity().multiply(request.getUnitPrice());
        }

        PurchaseRecord record = PurchaseRecord.builder()
                .organization(org)
                .supplier(supplier)
                .product(product)
                .medication(medication)
                .purchaseDate(request.getPurchaseDate())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalAmount(total)
                .paymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "Paid")
                .notes(request.getNotes())
                .build();

        return mapToResponse(purchaseRecordRepository.save(record));
    }

    @Transactional
    public PurchaseRecordDTO.Response update(UUID id, PurchaseRecordDTO.UpdateRequest request) {
        PurchaseRecord record = purchaseRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compra no encontrada con ID: " + id));

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
            record.setSupplier(supplier);
        }

        if (request.getPurchaseDate() != null) record.setPurchaseDate(request.getPurchaseDate());
        if (request.getQuantity() != null) record.setQuantity(request.getQuantity());
        if (request.getUnitPrice() != null) record.setUnitPrice(request.getUnitPrice());
        if (request.getTotalAmount() != null) record.setTotalAmount(request.getTotalAmount());
        if (request.getPaymentStatus() != null) record.setPaymentStatus(request.getPaymentStatus());
        if (request.getNotes() != null) record.setNotes(request.getNotes());

        return mapToResponse(purchaseRecordRepository.save(record));
    }

    @Transactional
    public void delete(UUID id) {
        if (!purchaseRecordRepository.existsById(id)) {
            throw new EntityNotFoundException("Compra no encontrada con ID: " + id);
        }
        purchaseRecordRepository.deleteById(id);
    }

    private PurchaseRecordDTO.Response mapToResponse(PurchaseRecord p) {
        return PurchaseRecordDTO.Response.builder()
                .id(p.getId())
                .organizationId(p.getOrganization().getId())
                .supplierId(p.getSupplier() != null ? p.getSupplier().getId() : null)
                .supplierName(p.getSupplier() != null ? p.getSupplier().getName() : null)
                .productId(p.getProduct() != null ? p.getProduct().getId() : null)
                .productName(p.getProduct() != null ? p.getProduct().getName() : null)
                .medicationId(p.getMedication() != null ? p.getMedication().getId() : null)
                .medicationName(p.getMedication() != null ? p.getMedication().getName() : null)
                .purchaseDate(p.getPurchaseDate())
                .quantity(p.getQuantity())
                .unitPrice(p.getUnitPrice())
                .totalAmount(p.getTotalAmount())
                .paymentStatus(p.getPaymentStatus())
                .notes(p.getNotes())
                .createdAt(p.getCreatedAt())
                .build();
    }
}