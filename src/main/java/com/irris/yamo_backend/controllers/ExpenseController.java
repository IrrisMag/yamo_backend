package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.Expense;
import com.irris.yamo_backend.repositories.ExpenseRepository;
import com.irris.yamo_backend.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;

    @PostMapping("/expenses")
    public ResponseEntity<Expense> create(@RequestBody Expense e) {
        Expense saved = expenseRepository.save(e);
        return ResponseEntity.created(URI.create("/api/expenses/" + saved.getId())).body(saved);
    }

    @GetMapping("/expenses")
    public List<Expense> list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                              @RequestParam(required = false) Expense.Category category) {
        if (from != null && to != null) return expenseRepository.findByDateBetween(from, to);
        if (category != null) return expenseRepository.findByCategory(category);
        return expenseRepository.findAll();
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Treasury summaries
    @GetMapping("/treasury/summary")
    public Map<String, Object> treasurySummary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var payments = paymentRepository.findAll();
        double totalPayments = payments.stream()
                .filter(p -> p.getPaymentDate() != null && !p.getPaymentDate().toLocalDate().isBefore(from) && !p.getPaymentDate().toLocalDate().isAfter(to))
                .map(p -> p.getAmount() == null ? 0.0 : p.getAmount())
                .mapToDouble(Double::doubleValue).sum();
        var expenses = expenseRepository.findByDateBetween(from, to);
        double totalExpenses = expenses.stream().map(e -> e.getAmount() == null ? 0.0 : e.getAmount()).mapToDouble(Double::doubleValue).sum();
        return Map.of(
                "from", from,
                "to", to,
                "payments", totalPayments,
                "expenses", totalExpenses,
                "net", totalPayments - totalExpenses
        );
    }

    @GetMapping("/treasury/daily")
    public List<Map<String, Object>> treasuryDaily(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        java.util.Map<LocalDate, Double> pay = new java.util.HashMap<>();
        for (var p : paymentRepository.findAll()) {
            if (p.getPaymentDate() == null) continue;
            LocalDate d = p.getPaymentDate().toLocalDate();
            if (d.isBefore(from) || d.isAfter(to)) continue;
            pay.merge(d, p.getAmount() == null ? 0.0 : p.getAmount(), Double::sum);
        }
        java.util.Map<LocalDate, Double> exp = new java.util.HashMap<>();
        for (var e : expenseRepository.findByDateBetween(from, to)) {
            exp.merge(e.getDate(), e.getAmount() == null ? 0.0 : e.getAmount(), Double::sum);
        }
        java.util.List<Map<String, Object>> out = new java.util.ArrayList<>();
        LocalDate d = from;
        while (!d.isAfter(to)) {
            double p = pay.getOrDefault(d, 0.0);
            double e = exp.getOrDefault(d, 0.0);
            out.add(Map.of("date", d, "payments", p, "expenses", e, "net", p - e));
            d = d.plusDays(1);
        }
        return out;
    }
}
