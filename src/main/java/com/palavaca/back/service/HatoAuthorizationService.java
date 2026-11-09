package com.palavaca.back.service;

import com.palavaca.back.model.Animal;
import com.palavaca.back.model.Customer;
import com.palavaca.back.model.Farm;
import com.palavaca.back.model.Lot;
import com.palavaca.back.model.Movement;
import com.palavaca.back.model.Paddock;
import com.palavaca.back.model.ReproductiveRecord;
import com.palavaca.back.model.SalesRecord;
import com.palavaca.back.model.Treatment;
import com.palavaca.back.model.User;
import com.palavaca.back.model.WeightRecord;
import com.palavaca.back.repository.AnimalRepository;
import com.palavaca.back.repository.CustomerRepository;
import com.palavaca.back.repository.FarmRepository;
import com.palavaca.back.repository.LotRepository;
import com.palavaca.back.repository.MovementRepository;
import com.palavaca.back.repository.PaddockRepository;
import com.palavaca.back.repository.ReproductiveRecordRepository;
import com.palavaca.back.repository.SalesRecordRepository;
import com.palavaca.back.repository.TreatmentRepository;
import com.palavaca.back.repository.UserRepository;
import com.palavaca.back.repository.WeightRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HatoAuthorizationService {

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final AnimalRepository animalRepository;
    private final LotRepository lotRepository;
    private final PaddockRepository paddockRepository;
    private final WeightRecordRepository weightRecordRepository;
    private final ReproductiveRecordRepository reproductiveRecordRepository;
    private final TreatmentRepository treatmentRepository;
    private final SalesRecordRepository salesRecordRepository;
    private final MovementRepository movementRepository;
    private final CustomerRepository customerRepository;

    public User currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    public boolean isOrganizationAdmin(User user) {
        return user.getRole() != null
                && user.getRole().toLowerCase(Locale.ROOT).contains("admin");
    }

    public void requireOrganization(Authentication authentication, UUID organizationId) {
        User user = currentUser(authentication);
        if (organizationId == null || !user.getOrganization().getId().equals(organizationId)) {
            forbidden();
        }
    }

    public Farm requireFarm(Authentication authentication, UUID farmId) {
        User user = currentUser(authentication);
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new EntityNotFoundException("Finca no encontrada"));
        if (!farm.getOrganization().getId().equals(user.getOrganization().getId())
                || !"active".equalsIgnoreCase(farm.getStatus())
                || (!isOrganizationAdmin(user)
                && (user.getFarm() == null || !farm.getId().equals(user.getFarm().getId())))) {
            forbidden();
        }
        return farm;
    }

    public Animal requireAnimal(Authentication authentication, UUID animalId) {
        User user = currentUser(authentication);
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new EntityNotFoundException("Animal no encontrado"));
        if (!animal.getOrganization().getId().equals(user.getOrganization().getId())) forbidden();
        if (animal.getFarm() != null) requireFarm(authentication, animal.getFarm().getId());
        else if (!isOrganizationAdmin(user)) forbidden();
        return animal;
    }

    public Lot requireLot(Authentication authentication, UUID lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new EntityNotFoundException("Lote no encontrado"));
        requireFarm(authentication, lot.getFarm().getId());
        return lot;
    }

    public Paddock requirePaddock(Authentication authentication, UUID paddockId) {
        Paddock paddock = paddockRepository.findById(paddockId)
                .orElseThrow(() -> new EntityNotFoundException("Potrero no encontrado"));
        requireFarm(authentication, paddock.getFarm().getId());
        return paddock;
    }

    public WeightRecord requireWeightRecord(Authentication authentication, UUID id) {
        WeightRecord record = weightRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de peso no encontrado"));
        requireAnimal(authentication, record.getAnimal().getId());
        return record;
    }

    public ReproductiveRecord requireReproductiveRecord(Authentication authentication, UUID id) {
        ReproductiveRecord record = reproductiveRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro reproductivo no encontrado"));
        requireAnimal(authentication, record.getAnimal().getId());
        return record;
    }

    public Treatment requireTreatment(Authentication authentication, UUID id) {
        Treatment treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tratamiento no encontrado"));
        requireAnimal(authentication, treatment.getAnimal().getId());
        return treatment;
    }

    public SalesRecord requireSale(Authentication authentication, UUID id) {
        User user = currentUser(authentication);
        SalesRecord sale = salesRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada"));
        if (!sale.getOrganization().getId().equals(user.getOrganization().getId())) forbidden();
        if (sale.getFarm() != null) requireFarm(authentication, sale.getFarm().getId());
        else if (!isOrganizationAdmin(user)) forbidden();
        return sale;
    }

    public Movement requireMovement(Authentication authentication, UUID id) {
        Movement movement = movementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento no encontrado"));
        requireFarm(authentication, movement.getFarm().getId());
        return movement;
    }

    public Customer requireCustomer(Authentication authentication, UUID id) {
        User user = currentUser(authentication);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        if (!customer.getOrganization().getId().equals(user.getOrganization().getId())) forbidden();
        return customer;
    }

    private void forbidden() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El recurso está fuera de tu alcance autorizado.");
    }
}
