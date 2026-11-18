package com.palavaca.back.service;

import com.palavaca.back.model.Animal;
import com.palavaca.back.model.FeedingRecord;
import com.palavaca.back.model.ProductionRecord;
import com.palavaca.back.model.PurchaseRecord;
import com.palavaca.back.model.SalesRecord;
import com.palavaca.back.repository.AnimalRepository;
import com.palavaca.back.repository.FeedingRecordRepository;
import com.palavaca.back.repository.ProductionRecordRepository;
import com.palavaca.back.repository.PurchaseRecordRepository;
import com.palavaca.back.repository.SalesRecordRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Supplier;

/**
 * Genera los movimientos financieros que faltan para los registros operativos creados antes de la
 * integración con Finanzas (datos legacy). Reutiliza {@link FinanceIntegrationService} para que las
 * categorías, montos y {@code reference_type}/{@code reference_id} sean exactamente los que produce
 * el alta normal: no se inventan reglas nuevas y no se duplican movimientos.
 *
 * <p>Es idempotente: solo busca los registros que aún no tienen movimiento enlazado y el propio
 * servicio vuelve a comprobar la ausencia de movimiento antes de crearlo, por lo que una segunda
 * ejecución no hace trabajo. Cada categoría se procesa en su propia transacción para que un registro
 * problemático no impida recuperar el resto ni bloquee el arranque.
 */
@Component
@RequiredArgsConstructor
public class FinanceLegacyBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FinanceLegacyBackfill.class);

    private final AnimalRepository animalRepository;
    private final PurchaseRecordRepository purchaseRecordRepository;
    private final FeedingRecordRepository feedingRecordRepository;
    private final SalesRecordRepository salesRecordRepository;
    private final ProductionRecordRepository productionRecordRepository;
    private final FinanceIntegrationService financeIntegrationService;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void run(ApplicationArguments args) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        int processed = 0;
        processed += backfill("gastos de adquisición de animales", transaction, this::backfillAnimals);
        processed += backfill("gastos de compras", transaction, this::backfillPurchases);
        processed += backfill("gastos de alimentación", transaction, this::backfillFeeding);
        processed += backfill("ingresos de ventas", transaction, this::backfillSales);
        processed += backfill("ingresos de producción", transaction, this::backfillProduction);
        if (processed > 0) {
            log.info("Backfill financiero: se procesaron {} registros legacy sin movimiento asociado", processed);
        }
    }

    private int backfill(String description, TransactionTemplate transaction, Supplier<Integer> step) {
        try {
            Integer processed = transaction.execute(status -> step.get());
            return processed == null ? 0 : processed;
        } catch (RuntimeException exception) {
            log.warn("No se pudieron generar los movimientos financieros de {}", description, exception);
            return 0;
        }
    }

    private int backfillAnimals() {
        List<Animal> pending = animalRepository
                .findWithoutAcquisitionExpense(FinanceIntegrationService.REFERENCE_ANIMAL);
        pending.forEach(financeIntegrationService::recordExpenseForAnimal);
        return pending.size();
    }

    private int backfillPurchases() {
        List<PurchaseRecord> pending = purchaseRecordRepository
                .findWithoutPurchaseExpense(FinanceIntegrationService.REFERENCE_PURCHASE);
        pending.forEach(financeIntegrationService::recordExpenseForPurchase);
        return pending.size();
    }

    private int backfillFeeding() {
        List<FeedingRecord> pending = feedingRecordRepository
                .findWithoutFeedingExpense(FinanceIntegrationService.REFERENCE_FEEDING);
        pending.forEach(financeIntegrationService::recordExpenseForFeeding);
        return pending.size();
    }

    private int backfillSales() {
        List<SalesRecord> pending = salesRecordRepository
                .findWithoutSaleIncome(FinanceIntegrationService.REFERENCE_SALE);
        pending.forEach(financeIntegrationService::recordIncomeForSale);
        return pending.size();
    }

    private int backfillProduction() {
        List<ProductionRecord> pending = productionRecordRepository
                .findWithoutProductionIncome(FinanceIntegrationService.REFERENCE_PRODUCTION);
        pending.forEach(financeIntegrationService::recordIncomeForProduction);
        return pending.size();
    }
}
