package com.palavaca.back.service;

import com.palavaca.back.dto.FinancialRecordDTO;
import com.palavaca.back.model.FinancialRecord;
import com.palavaca.back.model.Organization;
import com.palavaca.back.repository.FinancialRecordRepository;
import com.palavaca.back.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<FinancialRecordDTO.Response> findByOrganizationId(UUID organizationId) {
        return financialRecordRepository.findByOrganizationIdOrderByRecordDateDesc(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FinancialRecordDTO.Response findById(UUID id) {
        FinancialRecord record = financialRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento financiero no encontrado con ID: " + id));
        return mapToResponse(record);
    }

    @Transactional
    public FinancialRecordDTO.Response create(FinancialRecordDTO.CreateRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + request.getOrganizationId()));

        FinancialRecord record = FinancialRecord.builder()
                .organization(org)
                .recordDate(request.getRecordDate())
                .transactionType(request.getTransactionType().toUpperCase())
                .category(request.getCategory())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        return mapToResponse(financialRecordRepository.save(record));
    }

    @Transactional
    public FinancialRecordDTO.Response update(UUID id, FinancialRecordDTO.UpdateRequest request) {
        FinancialRecord record = financialRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento financiero no encontrado con ID: " + id));

        if (request.getRecordDate() != null) record.setRecordDate(request.getRecordDate());
        if (request.getTransactionType() != null) record.setTransactionType(request.getTransactionType().toUpperCase());
        if (request.getCategory() != null) record.setCategory(request.getCategory());
        if (request.getAmount() != null) record.setAmount(request.getAmount());
        if (request.getDescription() != null) record.setDescription(request.getDescription());

        return mapToResponse(financialRecordRepository.save(record));
    }

    @Transactional
    public void delete(UUID id) {
        if (!financialRecordRepository.existsById(id)) {
            throw new EntityNotFoundException("Movimiento financiero no encontrado con ID: " + id);
        }
        financialRecordRepository.deleteById(id);
    }

    private FinancialRecordDTO.Response mapToResponse(FinancialRecord f) {
        return FinancialRecordDTO.Response.builder()
                .id(f.getId())
                .organizationId(f.getOrganization().getId())
                .recordDate(f.getRecordDate())
                .transactionType(f.getTransactionType())
                .category(f.getCategory())
                .amount(f.getAmount())
                .description(f.getDescription())
                .createdAt(f.getCreatedAt())
                .build();
    }
}