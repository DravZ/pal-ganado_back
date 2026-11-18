package com.palavaca.back.service;

import com.palavaca.back.model.SyncChange;
import com.palavaca.back.repository.SyncChangeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Rellena la organización y la finca de los cambios de sincronización históricos que quedaron con
 * {@code organization_id} nulo por el defecto de lectura de asociaciones perezosas del catálogo.
 * Sin este relleno, {@code /sync/changes} seguiría omitiendo esos registros porque la consulta exige
 * coincidencia de organización.
 *
 * <p>Es idempotente: solo consulta las filas con organización nula, por lo que tras la primera
 * ejecución no hace trabajo. Los cambios cuyos registros de origen ya no existen se omiten (no se
 * puede reconstruir su alcance y devolverlos crearía registros fantasma en los clientes). Cada
 * recurso se procesa en su propia transacción para que un recurso problemático no impida reconstruir
 * el resto ni bloquear el arranque.
 */
@Component
@RequiredArgsConstructor
public class SyncChangeScopeBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SyncChangeScopeBackfill.class);

    private final EntityManager entityManager;
    private final SyncChangeRepository syncChangeRepository;
    private final SyncResourceCatalog resourceCatalog;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void run(ApplicationArguments args) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        int repaired = 0;
        int orphaned = 0;
        for (SyncResourceCatalog.Resource resource : resourceCatalog.resources()) {
            if (resource.organizationPath() == null) continue;
            try {
                int[] counts = transaction.execute(status -> backfillResource(resource));
                if (counts != null) {
                    repaired += counts[0];
                    orphaned += counts[1];
                }
            } catch (RuntimeException exception) {
                log.warn("No se pudieron reconstruir los cambios de sincronización de {}", resource.name(),
                        exception);
            }
        }
        if (repaired > 0) {
            log.info("Se reconstruyó el alcance de {} cambios de sincronización históricos", repaired);
        }
        if (orphaned > 0) {
            log.info("Se omitieron {} cambios de sincronización históricos cuyos registros ya no existen",
                    orphaned);
        }
    }

    private int[] backfillResource(SyncResourceCatalog.Resource resource) {
        List<SyncChange> pending = syncChangeRepository
                .findByResourceTypeAndOrganizationIdIsNull(resource.name());
        if (pending.isEmpty()) return new int[] {0, 0};

        int repaired = 0;
        int orphaned = 0;
        for (SyncChange change : pending) {
            Object entity = entityManager.find(resource.entityType(), change.getRecordId());
            if (entity == null) {
                orphaned++;
                continue;
            }
            change.setOrganizationId(resourceCatalog.organizationId(entity, resource));
            change.setFarmId(resourceCatalog.farmId(entity, resource));
            repaired++;
        }
        syncChangeRepository.saveAll(pending);
        return new int[] {repaired, orphaned};
    }
}
