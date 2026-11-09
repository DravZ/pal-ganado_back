package com.palavaca.back.service;

import com.palavaca.back.dto.SyncDTO;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.SyncChange;
import com.palavaca.back.model.User;
import com.palavaca.back.repository.FarmRepository;
import com.palavaca.back.repository.SyncChangeRepository;
import com.palavaca.back.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfflineSyncService {

    private static final int MAX_PAGE_SIZE = 200;

    private final EntityManager entityManager;
    private final FarmRepository farmRepository;
    private final SyncChangeRepository syncChangeRepository;
    private final UserRepository userRepository;
    private final SyncResourceCatalog resourceCatalog;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public SyncDTO.BootstrapResponse bootstrap(String email) {
        User user = findUser(email);
        AccessScope scope = accessScope(user);
        long cursor = syncChangeRepository.currentCursor();
        Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();

        for (SyncResourceCatalog.Resource resource : resourceCatalog.resources()) {
            if (resource.administratorsOnly() && !scope.organizationAdmin()) {
                if (!resource.entityType().equals(User.class)) continue;
            }
            if (resource.farmPath() != null && !scope.organizationAdmin() && scope.farmIds().isEmpty()) {
                tables.put(resource.name(), List.of());
                continue;
            }

            List<Map<String, Object>> rows = loadAuthorizedRows(resource, scope, resource.entityType()).stream()
                    .map(row -> resourceCatalog.project(row, resource))
                    .toList();
            tables.put(resource.name(), rows);
        }

        return new SyncDTO.BootstrapResponse(
                user.getId(),
                user.getOrganization().getId(),
                scope.farmIds(),
                cursor,
                Instant.now(),
                tables);
    }

    @Transactional(readOnly = true)
    public SyncDTO.ChangePage changes(String email, long cursor, Long requestedUpperBound, int pageSize) {
        if (cursor < 0 || (requestedUpperBound != null && requestedUpperBound < cursor)) {
            throw new IllegalArgumentException("El cursor de sincronización no es válido");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y " + MAX_PAGE_SIZE);
        }

        User user = findUser(email);
        AccessScope scope = accessScope(user);
        long upperBound = requestedUpperBound == null
                ? syncChangeRepository.currentCursor()
                : requestedUpperBound;
        if (upperBound > syncChangeRepository.currentCursor()) {
            throw new IllegalArgumentException("El límite del cursor no es válido");
        }

        PageRequest pageRequest = PageRequest.of(0, pageSize + 1);
        List<SyncChange> rows = scope.farmIds().isEmpty()
                ? syncChangeRepository.findPageWithoutFarmScope(
                        cursor, upperBound, user.getOrganization().getId(),
                        scope.organizationAdmin(), user.getId(), pageRequest)
                : syncChangeRepository.findPageForScope(
                        cursor, upperBound, user.getOrganization().getId(), scope.farmIds(),
                        scope.organizationAdmin(), user.getId(), pageRequest);

        boolean hasMore = rows.size() > pageSize;
        List<SyncChange> pageRows = hasMore ? rows.subList(0, pageSize) : rows;
        long nextCursor = hasMore
                ? pageRows.get(pageRows.size() - 1).getId()
                : upperBound;
        List<SyncDTO.Change> changes = pageRows.stream().map(this::mapChange).toList();

        return new SyncDTO.ChangePage(user.getId(), cursor, upperBound, nextCursor, hasMore, changes);
    }

    private <T> List<T> loadAuthorizedRows(
            SyncResourceCatalog.Resource resource,
            AccessScope scope,
            Class<T> entityType) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(entityType);
        Root<T> root = query.from(entityType);
        List<Predicate> predicates = new ArrayList<>();

        if (resource.organizationPath() != null) {
            predicates.add(criteriaBuilder.equal(path(root, resource.organizationPath()), scope.organizationId()));
        }

        if (resource.entityType().equals(User.class) && !scope.organizationAdmin()) {
            predicates.add(criteriaBuilder.equal(root.get("id"), scope.userId()));
        }

        if (resource.farmPath() != null && !scope.organizationAdmin()) {
            predicates.add(path(root, resource.farmPath()).in(scope.farmIds()));
        }

        if (resource.entityType().equals(com.palavaca.back.model.Organization.class)) {
            predicates.add(criteriaBuilder.equal(root.get("id"), scope.organizationId()));
        }

        query.select(root).where(predicates.toArray(Predicate[]::new));
        return entityManager.createQuery(query).getResultList();
    }

    private Path<?> path(Root<?> root, String dottedPath) {
        Path<?> path = root;
        for (String segment : dottedPath.split("\\.")) path = path.get(segment);
        return path;
    }

    private SyncDTO.Change mapChange(SyncChange change) {
        JsonNode payload = null;
        if (change.getPayload() != null) {
            try {
                payload = objectMapper.readTree(change.getPayload());
            } catch (JacksonException exception) {
                throw new IllegalStateException("El cambio almacenado contiene un payload inválido", exception);
            }
        }
        return new SyncDTO.Change(
                change.getId(), change.getResourceType(), change.getRecordId(),
                change.getOperation(), payload, change.getOccurredAt());
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("El usuario autenticado ya no existe"));
    }

    private AccessScope accessScope(User user) {
        boolean organizationAdmin = user.getRole() != null
                && user.getRole().toLowerCase(java.util.Locale.ROOT).contains("admin");
        List<UUID> farmIds;
        if (organizationAdmin) {
            farmIds = farmRepository.findByOrganizationId(user.getOrganization().getId()).stream()
                    .filter(farm -> "active".equalsIgnoreCase(farm.getStatus()))
                    .map(Farm::getId)
                    .toList();
        } else if (user.getFarm() != null
                && user.getFarm().getOrganization().getId().equals(user.getOrganization().getId())
                && "active".equalsIgnoreCase(user.getFarm().getStatus())) {
            farmIds = List.of(user.getFarm().getId());
        } else {
            farmIds = List.of();
        }
        return new AccessScope(user.getId(), user.getOrganization().getId(), farmIds, organizationAdmin);
    }

    private record AccessScope(UUID userId, UUID organizationId, List<UUID> farmIds,
                               boolean organizationAdmin) {}
}
