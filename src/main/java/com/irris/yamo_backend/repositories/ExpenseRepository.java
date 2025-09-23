package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByDateBetween(LocalDate from, LocalDate to);
    List<Expense> findByCategory(Expense.Category category);
}
