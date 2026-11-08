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
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PurchaseRecordService {

    private final PurchaseRecordRepository purchaseRecordRepository;
    private final OrganizationRepository organizationRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final MedicationRepository medicationRepository;
    private final FarmRepository farmRepository;
    private final PurchaseLineRepository purchaseLineRepository;

    @Transactional(readOnly = true)
    public List<PurchaseRecordDTO.Response> findByOrganizationId(UUID organizationId) {
        return purchaseRecordRepository.findByOrganizationIdOrderByPurchaseDateDesc(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseRecordDTO.Response> findByFarmId(UUID farmId) {
        return purchaseRecordRepository.findByFarmIdOrderByPurchaseDateDesc(farmId).stream()
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
        Farm farm = request.getFarmId() == null ? null : farmRepository.findById(request.getFarmId())
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada con ID: " + request.getFarmId()));
        UUID organizationId = request.getOrganizationId() != null
                ? request.getOrganizationId() : farm == null ? null : farm.getOrganization().getId();
        if (organizationId == null) throw new IllegalArgumentException("La organización o finca es obligatoria");
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + organizationId));

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

        BigDecimal subtotal = request.getSubtotal();
        BigDecimal total = request.getTotalAmount() != null ? request.getTotalAmount() : request.getTotal();
        if (subtotal == null && request.getLines() != null) {
            subtotal = request.getLines().stream()
                    .map(line -> line.getSubtotal() != null ? line.getSubtotal()
                            : line.getQuantity() != null && line.getUnitPrice() != null
                            ? line.getQuantity().multiply(line.getUnitPrice()) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (total == null && request.getQuantity() != null && request.getUnitPrice() != null) {
            total = request.getQuantity().multiply(request.getUnitPrice());
        }
        if (total == null && subtotal != null) total = subtotal.add(request.getTax() == null ? BigDecimal.ZERO : request.getTax());
        if (total == null) throw new IllegalArgumentException("El monto total de la compra es obligatorio");

        PurchaseRecord record = PurchaseRecord.builder()
                .organization(org)
                .farm(farm)
                .invoiceNumber(request.getInvoiceNumber())
                .supplier(supplier)
                .product(product)
                .medication(medication)
                .purchaseDate(request.getPurchaseDate())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .subtotal(subtotal)
                .tax(request.getTax())
                .totalAmount(total)
                .paymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "Paid")
                .notes(request.getNotes())
                .build();

        PurchaseRecord saved = purchaseRecordRepository.save(record);
        if (request.getLines() != null) {
            for (PurchaseRecordDTO.LineRequest line : request.getLines()) {
                Product lineProduct = productRepository.findById(line.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
                purchaseLineRepository.save(PurchaseLine.builder()
                        .purchase(saved)
                        .product(lineProduct)
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .subtotal(line.getSubtotal())
                        .build());
            }
        }
        return mapToResponse(saved);
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
        if (request.getInvoiceNumber() != null) record.setInvoiceNumber(request.getInvoiceNumber());

        if (request.getPurchaseDate() != null) record.setPurchaseDate(request.getPurchaseDate());
        if (request.getQuantity() != null) record.setQuantity(request.getQuantity());
        if (request.getUnitPrice() != null) record.setUnitPrice(request.getUnitPrice());
        if (request.getSubtotal() != null) record.setSubtotal(request.getSubtotal());
        if (request.getTax() != null) record.setTax(request.getTax());
        if (request.getTotalAmount() != null) record.setTotalAmount(request.getTotalAmount());
        if (request.getTotal() != null) record.setTotalAmount(request.getTotal());
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
                .farmId(p.getFarm() != null ? p.getFarm().getId() : null)
                .invoiceNumber(p.getInvoiceNumber())
                .supplierId(p.getSupplier() != null ? p.getSupplier().getId() : null)
                .supplierName(p.getSupplier() != null ? p.getSupplier().getName() : null)
                .productId(p.getProduct() != null ? p.getProduct().getId() : null)
                .productName(p.getProduct() != null ? p.getProduct().getName() : null)
                .medicationId(p.getMedication() != null ? p.getMedication().getId() : null)
                .medicationName(p.getMedication() != null ? p.getMedication().getName() : null)
                .purchaseDate(p.getPurchaseDate())
                .quantity(p.getQuantity())
                .unitPrice(p.getUnitPrice())
                .subtotal(p.getSubtotal())
                .tax(p.getTax())
                .total(p.getTotalAmount())
                .totalAmount(p.getTotalAmount())
                .paymentStatus(p.getPaymentStatus())
                .notes(p.getNotes())
                .lines(purchaseLineRepository.findByPurchaseId(p.getId()).stream()
                        .map(line -> PurchaseRecordDTO.LineResponse.builder()
                                .id(line.getId())
                                .productId(line.getProduct().getId())
                                .quantity(line.getQuantity())
                                .unitPrice(line.getUnitPrice())
                                .subtotal(line.getSubtotal())
                                .build())
                        .toList())
                .createdAt(p.getCreatedAt())
                .build();
    }
}