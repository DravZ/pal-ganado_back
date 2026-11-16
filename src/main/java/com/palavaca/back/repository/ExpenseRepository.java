package com.palavaca.back.repository;

import com.palavaca.back.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("select e from Expense e where e.farm.id = :farmId and e.deletedAt is null order by e.expenseDate desc")
    List<Expense> findByFarmIdOrderByExpenseDateDesc(@Param("farmId") UUID farmId);

    List<Expense> findByReferenceTypeAndReferenceIdAndDeletedAtIsNull(String referenceType, UUID referenceId);
}
