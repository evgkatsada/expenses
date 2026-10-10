package com.example.expenses.repository;

import com.example.expenses.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByDateBetweenOrderByDateDescIdDesc(LocalDate startDate, LocalDate endDate);

    List<Expense> findAllByOrderByDateDescIdDesc();
}
