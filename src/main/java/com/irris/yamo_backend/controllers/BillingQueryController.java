package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.entities.Invoice;
import com.irris.yamo_backend.repositories.CustomerRepository;
import com.irris.yamo_backend.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingQueryController {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    @GetMapping("/customers/{customerId}/balance")
    public ResponseEntity<?> customerBalance(@PathVariable Long customerId) {
        Customer c = customerRepository.findById(customerId).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        double credit = c.getCreditBalance() == null ? 0.0 : c.getCreditBalance();
        List<Invoice> invoices = invoiceRepository.findByCustomer(c);
        List<Map<String, Object>> unpaid = invoices.stream()
                .filter(inv -> inv.getStatus() != Invoice.Status.PAID && inv.getStatus() != Invoice.Status.CANCELLED)
                .sorted(Comparator.comparing(Invoice::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(inv -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("invoiceNumber", inv.getInvoiceNumber());
                    m.put("issueDate", inv.getIssueDate());
                    m.put("dueDate", inv.getDueDate());
                    m.put("status", inv.getStatus());
                    m.put("totalAmount", inv.getTotalAmount());
                    m.put("paidAmount", inv.getPaidAmount());
                    m.put("balanceAmount", inv.getBalanceAmount());
                    return m;
                })
                .collect(Collectors.toList());
        double totalUnpaid = invoices.stream()
                .filter(inv -> inv.getStatus() != Invoice.Status.PAID && inv.getStatus() != Invoice.Status.CANCELLED)
                .map(Invoice::getBalanceAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customerId", customerId);
        payload.put("creditBalance", credit);
        payload.put("totalUnpaid", totalUnpaid);
        payload.put("unpaidInvoices", unpaid);
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/receivables")
    public List<Map<String, Object>> receivables(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<Invoice> all = invoiceRepository.findAll();
        LocalDateTime fromDt = from == null ? null : from.atStartOfDay();
        LocalDateTime toDt = to == null ? null : to.atTime(LocalTime.MAX);

        return all.stream()
                .filter(inv -> inv.getStatus() != Invoice.Status.PAID && inv.getStatus() != Invoice.Status.CANCELLED)
                .filter(inv -> customerId == null || (inv.getCustomer() != null && Objects.equals(inv.getCustomer().getId(), customerId)))
                .filter(inv -> {
                    if (fromDt == null && toDt == null) return true;
                    LocalDateTime issue = inv.getIssueDate();
                    if (issue == null) return false;
                    boolean after = fromDt == null || !issue.isBefore(fromDt);
                    boolean before = toDt == null || !issue.isAfter(toDt);
                    return after && before;
                })
                .sorted(Comparator.comparing(Invoice::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(inv -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("customerId", inv.getCustomer() != null ? inv.getCustomer().getId() : null);
                    m.put("invoiceNumber", inv.getInvoiceNumber());
                    m.put("issueDate", inv.getIssueDate());
                    m.put("dueDate", inv.getDueDate());
                    m.put("status", inv.getStatus());
                    m.put("totalAmount", inv.getTotalAmount());
                    m.put("paidAmount", inv.getPaidAmount());
                    m.put("balanceAmount", inv.getBalanceAmount());
                    return m;
                })
                .collect(Collectors.toList());
    }
}
