package com.palavaca.back.service;

import com.palavaca.back.model.*;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SyncResourceCatalog {

    public record Resource(String name, Class<?> entityType, String organizationPath,
                           String farmPath, boolean administratorsOnly) {}

    private static final List<Resource> RESOURCES = List.of(
            new Resource("organizations", Organization.class, "id", null, false),
            new Resource("users", User.class, "organization.id", null, true),
            new Resource("farms", Farm.class, "organization.id", "id", false),
            new Resource("animals", Animal.class, "organization.id", "farm.id", false),
            new Resource("animal_identifiers", AnimalIdentifier.class, "animal.organization.id", "animal.farm.id", false),
            new Resource("paddocks", Paddock.class, "farm.organization.id", "farm.id", false),
            new Resource("lots", Lot.class, "farm.organization.id", "farm.id", false),
            new Resource("weight_records", WeightRecord.class, "animal.organization.id", "animal.farm.id", false),
            new Resource("reproductive_records", ReproductiveRecord.class, "animal.organization.id", "animal.farm.id", false),
            new Resource("health_records", HealthRecord.class, "animal.organization.id", "animal.farm.id", false),
            new Resource("treatments", Treatment.class, "animal.organization.id", "animal.farm.id", false),
            new Resource("vaccinations", Vaccination.class, "animal.organization.id", "animal.farm.id", false),
            new Resource("milk_productions", MilkProduction.class, "animal.organization.id", "animal.farm.id", false),
            new Resource("feeding_records", FeedingRecord.class, "farm.organization.id", "farm.id", false),
            new Resource("production_records", ProductionRecord.class, "farm.organization.id", "farm.id", false),
            new Resource("products", Product.class, "organization.id", null, false),
            new Resource("inventories", Inventory.class, "farm.organization.id", "farm.id", false),
            new Resource("inventory_movements", InventoryMovement.class, "inventory.farm.organization.id", "inventory.farm.id", false),
            new Resource("financial_records", FinancialRecord.class, "organization.id", "farm.id", false),
            new Resource("expenses", Expense.class, "farm.organization.id", "farm.id", false),
            new Resource("cost_allocations", CostAllocation.class, "farm.organization.id", "farm.id", false),
            new Resource("movements", Movement.class, "farm.organization.id", "farm.id", false),
            new Resource("movement_animals", MovementAnimal.class, "movement.farm.organization.id", "movement.farm.id", false),
            new Resource("alerts", Alert.class, "farm.organization.id", "farm.id", false),
            new Resource("customers", Customer.class, "organization.id", null, false),
            new Resource("suppliers", Supplier.class, "organization.id", null, false),
            new Resource("medications", Medication.class, null, null, false),
            new Resource("purchase_records", PurchaseRecord.class, "organization.id", "farm.id", false),
            new Resource("purchase_lines", PurchaseLine.class, "purchase.organization.id", "purchase.farm.id", false),
            new Resource("sales_records", SalesRecord.class, "organization.id", "farm.id", false),
            new Resource("sales_lines", SalesLine.class, "sale.organization.id", "sale.farm.id", false)
    );

    public List<Resource> resources() {
        return RESOURCES;
    }

    public Resource forEntity(Class<?> entityType) {
        return RESOURCES.stream()
                .filter(resource -> resource.entityType().isAssignableFrom(entityType)
                        || entityType.isAssignableFrom(resource.entityType()))
                .findFirst()
                .orElse(null);
    }

    public UUID idOf(Object entity) {
        if (entity == null) return null;
        Object id = property(entity, "id");
        return id instanceof UUID uuid ? uuid : null;
    }

    public UUID organizationId(Object entity, Resource resource) {
        Object value = path(entity, resource.organizationPath());
        if (value instanceof UUID uuid) return uuid;
        if (entity instanceof Organization organization) return organization.getId();
        return null;
    }

    public UUID farmId(Object entity, Resource resource) {
        Object value = path(entity, resource.farmPath());
        return value instanceof UUID uuid ? uuid : null;
    }

    public Map<String, Object> project(Object entity) {
        Resource resource = forEntity(entity.getClass());
        return project(entity, resource);
    }

    public Map<String, Object> project(Object entity, Resource resource) {
        Map<String, Object> projected = new LinkedHashMap<>();
        for (Field field : fields(entity.getClass())) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()
                    || field.isAnnotationPresent(Transient.class)
                    || field.getName().equalsIgnoreCase("passwordHash")) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (field.isAnnotationPresent(Id.class)) {
                    projected.put(columnName(field), value);
                } else if (field.isAnnotationPresent(JoinColumn.class)) {
                    projected.put(field.getAnnotation(JoinColumn.class).name(), idOf(value));
                } else if (field.isAnnotationPresent(Column.class)
                        || field.isAnnotationPresent(Version.class)) {
                    projected.put(columnName(field), value);
                }
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("No se pudo proyectar el recurso de sincronización", exception);
            }
        }
        if (resource != null) {
            UUID organizationId = organizationId(entity, resource);
            UUID farmId = farmId(entity, resource);
            if (organizationId != null) projected.put("scope_organization_id", organizationId);
            if (farmId != null) projected.put("scope_farm_id", farmId);
        }
        return projected;
    }

    private Object path(Object value, String propertyPath) {
        if (value == null || propertyPath == null) return null;
        Object current = value;
        for (String propertyName : propertyPath.split("\\.")) {
            current = property(current, propertyName);
            if (current == null) return null;
        }
        return current;
    }

    private Object property(Object value, String name) {
        if (value == null) return null;
        Class<?> type = value.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(value);
            } catch (NoSuchFieldException exception) {
                type = type.getSuperclass();
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("No se pudo inspeccionar un recurso de sincronización", exception);
            }
        }
        return null;
    }

    private List<Field> fields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            fields.addAll(List.of(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
    }

    private String columnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isBlank()) return column.name();
        return field.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
