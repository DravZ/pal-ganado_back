package com.palavaca.back.service;

import com.palavaca.back.model.SyncChange;
import com.palavaca.back.model.User;
import com.palavaca.back.repository.SyncChangeRepository;
import com.palavaca.back.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SyncChangeRecorder {

    private final SyncChangeRepository syncChangeRepository;
    private final UserRepository userRepository;
    private final SyncResourceCatalog resourceCatalog;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(Object entity, String operation) {
        SyncResourceCatalog.Resource resource = resourceCatalog.forEntity(entity.getClass());
        UUID recordId = resourceCatalog.idOf(entity);
        if (resource == null || recordId == null) return;

        String payload = null;
        if (!"DELETE".equals(operation)) {
            try {
                Map<String, Object> projection = resourceCatalog.project(entity, resource);
                payload = objectMapper.writeValueAsString(projection);
            } catch (JacksonException exception) {
                throw new IllegalStateException("No se pudo registrar el cambio del recurso", exception);
            }
        }

        UUID actorId = authenticatedUserId();
        entityManager.createNativeQuery("select pg_advisory_xact_lock(781204631)").getSingleResult();
        syncChangeRepository.save(SyncChange.builder()
                .organizationId(resourceCatalog.organizationId(entity, resource))
                .farmId(resourceCatalog.farmId(entity, resource))
                .actorId(actorId)
                .resourceType(resource.name())
                .recordId(recordId)
                .operation(operation)
                .payload(payload)
                .build());
    }

    private UUID authenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName())
                .map(User::getId)
                .orElse(null);
    }
}
