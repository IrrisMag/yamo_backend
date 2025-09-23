package com.irris.yamo_backend.services;

import com.irris.yamo_backend.entities.*;
import com.irris.yamo_backend.repositories.InvoiceRepository;
import com.irris.yamo_backend.repositories.OrderRepository;
import com.irris.yamo_backend.repositories.PaymentRepository;
import com.irris.yamo_backend.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvoicingService {
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;

    public Invoice createInvoiceForOrder(Long orderId, LocalDate dueDate) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setCustomer(order.getCustomer());
        invoice.setIssueDate(LocalDateTime.now());
        // If due date not provided, default to 3 days after issue (TODO: exclude Sundays/holidays when rules provided)
        invoice.setDueDate(dueDate != null ? dueDate : LocalDate.now().plusDays(3));
        invoice.setStatus(Invoice.Status.ISSUED);
        invoice.setTotalAmount(order.getTotalAmount() == null ? 0.0 : order.getTotalAmount());
        invoice.setPaidAmount(0.0);
        invoice.setBalanceAmount(invoice.getTotalAmount());
        invoice.setInvoiceNumber(nextInvoiceNumber());
        Invoice saved = invoiceRepository.save(invoice);

        // Auto-apply available customer credit to the invoice
        applyAvailableCreditToInvoice(saved);

        return saved;
    }

    public Payment recordPayment(Long customerId, Double amount, Payment.Method method, String reference, String invoiceNumber) {
        double incoming = amount == null ? 0.0 : amount;
        if (incoming < 0) throw new IllegalArgumentException("Payment amount cannot be negative");

        Customer customer = customerRepository.findById(customerId).orElseThrow();

        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(incoming);
        payment.setMethod(method);
        payment.setReference(reference);

        double remaining = incoming;
        if (invoiceNumber != null && !invoiceNumber.isBlank()) {
            Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber).orElseThrow();
            payment.setInvoice(invoice);
            remaining = applyPaymentToInvoice(invoice, remaining);
        }

        // If there is remaining money (overpayment or no target invoice), credit the customer account
        if (remaining > 0.0) {
            creditCustomerBalance(customer, remaining);
        }

        return paymentRepository.save(payment);
    }

    // Apply available customer credit to a newly created or existing invoice
    public void applyAvailableCreditToInvoice(Invoice invoice) {
        Customer customer = invoice.getCustomer();
        if (customer == null) return;
        Customer managed = customerRepository.findById(customer.getId()).orElseThrow();
        double credit = managed.getCreditBalance() == null ? 0.0 : managed.getCreditBalance();
        double balance = invoice.getBalanceAmount() == null ? 0.0 : invoice.getBalanceAmount();
        if (credit <= 0 || balance <= 0) return;

        double applied = Math.min(credit, balance);
        // Update invoice
        double newPaid = (invoice.getPaidAmount() == null ? 0.0 : invoice.getPaidAmount()) + applied;
        invoice.setPaidAmount(newPaid);
        double newBalance = balance - applied;
        invoice.setBalanceAmount(newBalance);
        invoice.setStatus(newBalance <= 0 ? Invoice.Status.PAID : Invoice.Status.PARTIALLY_PAID);
        invoiceRepository.save(invoice);

        // Update customer credit
        managed.setCreditBalance(credit - applied);
        customerRepository.save(managed);
    }

    // Allocates amount to invoice and returns remaining amount (if any)
    public double applyPaymentToInvoice(Invoice invoice, double amount) {
        if (amount <= 0) return 0.0;
        double paid = invoice.getPaidAmount() == null ? 0.0 : invoice.getPaidAmount();
        double balance = invoice.getBalanceAmount() == null ? 0.0 : invoice.getBalanceAmount();
        if (balance <= 0) return amount; // nothing to apply

        double applied = Math.min(balance, amount);
        double newPaid = paid + applied;
        double newBalance = balance - applied;
        invoice.setPaidAmount(newPaid);
        invoice.setBalanceAmount(newBalance);
        invoice.setStatus(newBalance <= 0 ? Invoice.Status.PAID : Invoice.Status.PARTIALLY_PAID);
        invoiceRepository.save(invoice);

        return amount - applied;
    }

    private void creditCustomerBalance(Customer customer, double credit) {
        double cur = customer.getCreditBalance() == null ? 0.0 : customer.getCreditBalance();
        customer.setCreditBalance(cur + credit);
        customerRepository.save(customer);
    }

    private String nextInvoiceNumber() {
        long next = invoiceRepository.findTopByOrderByIdDesc()
                .map(inv -> inv.getId() + 1)
                .orElse(1L);
        return String.format("FAC-%06d", next);
    }
}
