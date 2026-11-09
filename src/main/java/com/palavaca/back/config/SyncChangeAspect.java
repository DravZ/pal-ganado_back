package com.palavaca.back.config;

import com.palavaca.back.service.SyncChangeRecorder;
import com.palavaca.back.service.SyncResourceCatalog;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RequiredArgsConstructor
public class SyncChangeAspect {

    private final EntityManager entityManager;
    private final SyncChangeRecorder changeRecorder;
    private final SyncResourceCatalog resourceCatalog;

    @Around("execution(* org.springframework.data.repository.CrudRepository+.save*(..))")
    public Object recordSavedEntities(ProceedingJoinPoint joinPoint) throws Throwable {
        List<Object> entities = entityArguments(joinPoint.getArgs());
        List<Boolean> existed = entities.stream().map(this::alreadyExists).toList();
        Object result = joinPoint.proceed();
        entityManager.flush();
        List<Object> savedEntities = entityArguments(new Object[]{result});

        for (int index = 0; index < savedEntities.size(); index++) {
            Object entity = savedEntities.get(index);
            if (resourceCatalog.forEntity(entity.getClass()) == null) continue;
            boolean previouslyExisted = index < existed.size() && existed.get(index);
            changeRecorder.record(entity, previouslyExisted ? "UPDATE" : "CREATE");
        }
        return result;
    }

    @Around("execution(* com.palavaca.back.service..delete*(..))")
    public Object recordDeletedEntities(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID id = firstUuidArgument(joinPoint.getArgs());
        Object entity = id == null ? null : findTargetEntity(joinPoint.getTarget(), id);
        Object result = joinPoint.proceed();
        if (entity != null && resourceCatalog.forEntity(entity.getClass()) != null) {
            changeRecorder.record(entity, "DELETE");
        }
        return result;
    }

    private boolean alreadyExists(Object entity) {
        SyncResourceCatalog.Resource resource = resourceCatalog.forEntity(entity.getClass());
        UUID id = resourceCatalog.idOf(entity);
        return resource != null && id != null && entityManager.find(resource.entityType(), id) != null;
    }

    private List<Object> entityArguments(Object[] values) {
        List<Object> entities = new ArrayList<>();
        for (Object value : values) {
            if (value == null) continue;
            if (value instanceof Iterable<?> iterable) {
                iterable.forEach(item -> {
                    if (item != null && resourceCatalog.forEntity(item.getClass()) != null) entities.add(item);
                });
            } else if (resourceCatalog.forEntity(value.getClass()) != null) {
                entities.add(value);
            }
        }
        return entities;
    }

    private UUID firstUuidArgument(Object[] values) {
        for (Object value : values) if (value instanceof UUID uuid) return uuid;
        return null;
    }

    private Object findTargetEntity(Object target, UUID id) {
        if (target == null) return null;
        for (Field field : target.getClass().getDeclaredFields()) {
            if (!JpaRepository.class.isAssignableFrom(field.getType())) continue;
            Class<?> entityType = repositoryEntityType(field.getType());
            if (entityType == null || resourceCatalog.forEntity(entityType) == null) continue;
            try {
                field.setAccessible(true);
                Object repository = field.get(target);
                Object optional = repository.getClass().getMethod("findById", Object.class).invoke(repository, id);
                if (optional instanceof java.util.Optional<?> value) return value.orElse(null);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("No se pudo preparar el registro de sincronización", exception);
            }
        }
        return null;
    }

    private Class<?> repositoryEntityType(Class<?> repositoryType) {
        for (Type parent : repositoryType.getGenericInterfaces()) {
            if (!(parent instanceof ParameterizedType parameterizedType)) continue;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> rawClass
                    && JpaRepository.class.isAssignableFrom(rawClass)) {
                Type entityType = parameterizedType.getActualTypeArguments()[0];
                return entityType instanceof Class<?> entityClass ? entityClass : null;
            }
            if (rawType instanceof Class<?> rawClass) {
                Class<?> resolved = repositoryEntityType(rawClass);
                if (resolved != null) return resolved;
            }
        }
        return null;
    }
}
