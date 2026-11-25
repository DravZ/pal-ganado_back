package com.palavaca.back.service;

import com.palavaca.back.model.Paddock;
import com.palavaca.back.repository.AnimalRepository;
import com.palavaca.back.repository.PaddockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Control de capacidad de los potreros.
 *
 * <p>La ocupación de un potrero es la suma de los animales activos de los lotes que hoy tienen ese
 * potrero como ubicación actual. La capacidad máxima se mide en número de animales. Antes de
 * validar se bloquea la fila del potrero para que dos operaciones simultáneas no puedan exceder el
 * límite (control de concurrencia).</p>
 */
@Service
@RequiredArgsConstructor
public class PaddockCapacityService {

    private final PaddockRepository paddockRepository;
    private final AnimalRepository animalRepository;

    /** Bloquea el potrero indicado para validar su capacidad de forma exclusiva. */
    public Paddock lock(UUID paddockId) {
        return paddockRepository.findByIdForUpdate(paddockId)
                .orElseThrow(() -> new EntityNotFoundException("Potrero no encontrado con ID: " + paddockId));
    }

    /** Igual que {@link #lock(UUID)} pero admite un identificador nulo (sin potrero). */
    public Paddock lockIfPresent(UUID paddockId) {
        return paddockId == null ? null : lock(paddockId);
    }

    /** Ocupación actual del potrero: animales activos en los lotes ubicados en él. */
    public long occupancy(UUID paddockId) {
        return animalRepository.countActiveByPaddockId(paddockId);
    }

    /**
     * Verifica que el potrero no supere su capacidad máxima. Debe invocarse después de persistir el
     * cambio y con el potrero bloqueado. Una capacidad nula o menor o igual a cero significa que el
     * potrero no tiene un límite definido y no se valida.
     */
    public void assertNotOverCapacity(Paddock paddock) {
        if (paddock == null) return;
        Integer capacity = paddock.getCapacity();
        if (capacity == null || capacity <= 0) return;
        long occupied = occupancy(paddock.getId());
        if (occupied > capacity) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "El potrero \"" + paddock.getName() + "\" supera su capacidad máxima de "
                            + capacity + " animales (ocupación resultante: " + occupied + ").");
        }
    }
}
