package com.palavaca.back.service;

import com.palavaca.back.model.*;
import com.palavaca.back.repository.ExpenseRepository;
import com.palavaca.back.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Conecta los módulos operativos (compras, ventas, alimentación, producción y alta de
 * animales) con Finanzas. Cada operación relevante registra automáticamente su gasto o
 * ingreso, y su baja enlazada lo da de baja lógica junto con el movimiento financiero.
 *
 * Los movimientos generados se identifican con {@code reference_type}/{@code reference_id}
 * para evitar duplicados y poder encontrarlos al dar de baja el registro de origen.
 */
@Service
@RequiredArgsConstructor
public class FinanceIntegrationService {

    public static final String REFERENCE_PURCHASE = "PURCHASE";
    public static final String REFERENCE_SALE = "SALE";
    public static final String REFERENCE_FEEDING = "FEEDING";
    public static final String REFERENCE_PRODUCTION = "PRODUCTION";
    public static final String REFERENCE_ANIMAL = "ANIMAL";

    private final ExpenseRepository expenseRepository;
    private final FinancialRecordRepository financialRecordRepository;
    private final SyncChangeRecorder syncChangeRecorder;

    /** Compra de insumos o animales: se registra como gasto de la finca. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordExpenseForPurchase(PurchaseRecord purchase) {
        if (purchase == null || purchase.getFarm() == null || purchase.getId() == null) return;
        if (purchase.getTotalAmount() == null) return;
        if (!expenseRepository
                .findByReferenceTypeAndReferenceIdAndDeletedAtIsNull(REFERENCE_PURCHASE, purchase.getId())
                .isEmpty()) {
            return;
        }
        String detail = purchase.getInvoiceNumber() == null || purchase.getInvoiceNumber().isBlank()
                ? "Compra de insumos"
                : "Compra " + purchase.getInvoiceNumber();
        expenseRepository.save(Expense.builder()
                .farm(purchase.getFarm())
                .supplier(purchase.getSupplier())
                .expenseDate(purchase.getPurchaseDate())
                .category("Compra")
                .description(detail)
                .amount(purchase.getTotalAmount())
                .referenceType(REFERENCE_PURCHASE)
                .referenceId(purchase.getId())
                .status("PENDING")
                .build());
    }

    /** Venta de animales, leche o productos: se registra como ingreso de la organización. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordIncomeForSale(SalesRecord sale) {
        if (sale == null || sale.getOrganization() == null || sale.getId() == null) return;
        if (sale.getTotalAmount() == null) return;
        if (!financialRecordRepository
                .findByReferenceTypeAndReferenceIdAndDeletedAtIsNull(REFERENCE_SALE, sale.getId())
                .isEmpty()) {
            return;
        }
        financialRecordRepository.save(FinancialRecord.builder()
                .organization(sale.getOrganization())
                .farm(sale.getFarm())
                .recordDate(sale.getSaleDate())
                .transactionType("INCOME")
                .category("Venta")
                .amount(sale.getTotalAmount())
                .description("Venta registrada" + (sale.getSaleNumber() == null || sale.getSaleNumber().isBlank()
                        ? "" : " (" + sale.getSaleNumber() + ")"))
                .referenceType(REFERENCE_SALE)
                .referenceId(sale.getId())
                .build());
    }

    /** Alimentación del hato: se registra como gasto de la finca cuando tiene costo. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordExpenseForFeeding(FeedingRecord feeding) {
        if (feeding == null || feeding.getFarm() == null || feeding.getId() == null) return;
        if (feeding.getCost() == null || feeding.getCost().compareTo(BigDecimal.ZERO) <= 0) return;
        if (!expenseRepository
                .findByReferenceTypeAndReferenceIdAndDeletedAtIsNull(REFERENCE_FEEDING, feeding.getId())
                .isEmpty()) {
            return;
        }
        expenseRepository.save(Expense.builder()
                .farm(feeding.getFarm())
                .expenseDate(feeding.getFeedingDate())
                .category("Alimentación")
                .description("Alimentación" + (feeding.getProduct() == null
                        ? "" : " - " + feeding.getProduct().getName()))
                .amount(feeding.getCost())
                .referenceType(REFERENCE_FEEDING)
                .referenceId(feeding.getId())
                .status("PENDING")
                .build());
    }

    /** Producción (leche u otros): se registra como ingreso cuando reporta ingresos. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordIncomeForProduction(ProductionRecord production) {
        if (production == null || production.getFarm() == null || production.getId() == null) return;
        if (production.getRevenue() == null || production.getRevenue().compareTo(BigDecimal.ZERO) <= 0) return;
        if (!financialRecordRepository
                .findByReferenceTypeAndReferenceIdAndDeletedAtIsNull(REFERENCE_PRODUCTION, production.getId())
                .isEmpty()) {
            return;
        }
        financialRecordRepository.save(FinancialRecord.builder()
                .organization(production.getFarm().getOrganization())
                .farm(production.getFarm())
                .recordDate(production.getProductionDate())
                .transactionType("INCOME")
                .category("Producción")
                .amount(production.getRevenue())
                .description("Producción" + (production.getProductionType() == null
                        ? "" : " - " + production.getProductionType()))
                .referenceType(REFERENCE_PRODUCTION)
                .referenceId(production.getId())
                .build());
    }

    /** Alta de animal con costo de adquisición: se registra como gasto de la finca. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordExpenseForAnimal(Animal animal) {
        if (animal == null || animal.getFarm() == null || animal.getId() == null) return;
        if (animal.getAcquisitionCost() == null
                || animal.getAcquisitionCost().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (!expenseRepository
                .findByReferenceTypeAndReferenceIdAndDeletedAtIsNull(REFERENCE_ANIMAL, animal.getId())
                .isEmpty()) {
            return;
        }
        expenseRepository.save(Expense.builder()
                .farm(animal.getFarm())
                .expenseDate(animal.getAcquisitionDate())
                .category("Adquisición de animal")
                .description("Alta de animal " + animal.getTagNumber())
                .amount(animal.getAcquisitionCost())
                .referenceType(REFERENCE_ANIMAL)
                .referenceId(animal.getId())
                .status("PENDING")
                .build());
    }

    /**
     * Da de baja lógica los movimientos financieros enlazados a un registro operativo.
     * Registra explícitamente el DELETE de sincronización porque el guardado de una baja
     * lógica no dispara el aspecto de borrado.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void removeMovementsFor(String referenceType, UUID referenceId) {
        if (referenceType == null || referenceId == null) return;
        LocalDateTime now = LocalDateTime.now();
        for (Expense expense : expenseRepository
                .findByReferenceTypeAndReferenceIdAndDeletedAtIsNull(referenceType, referenceId)) {
            expense.setDeletedAt(now);
            expenseRepository.save(expense);
            syncChangeRecorder.record(expense, "DELETE");
        }
        for (FinancialRecord record : financialRecordRepository
                .findByReferenceTypeAndReferenceIdAndDeletedAtIsNull(referenceType, referenceId)) {
            record.setDeletedAt(now);
            financialRecordRepository.save(record);
            syncChangeRecorder.record(record, "DELETE");
        }
    }
}
