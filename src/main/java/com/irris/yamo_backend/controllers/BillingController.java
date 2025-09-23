package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.Invoice;
import com.irris.yamo_backend.entities.Payment;
import com.irris.yamo_backend.repositories.InvoiceRepository;
import com.irris.yamo_backend.repositories.PaymentRepository;
import com.irris.yamo_backend.services.InvoicingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {
    private final InvoicingService invoicingService;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @PostMapping("/orders/{orderId}/invoice")
    public ResponseEntity<Invoice> createInvoice(
            @PathVariable Long orderId,
            @RequestParam("dueDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate
    ) {
        Invoice invoice = invoicingService.createInvoiceForOrder(orderId, dueDate);
        return ResponseEntity.created(URI.create("/api/billing/invoices/" + invoice.getId())).body(invoice);
    }

    @GetMapping("/customers/{customerId}/invoices")
    public List<Invoice> listInvoices(@PathVariable Long customerId) {
        return invoiceRepository.findByCustomer(new com.irris.yamo_backend.entities.Customer(){ { setId(customerId);} });
    }

    @PostMapping("/payments")
    public ResponseEntity<Payment> recordPayment(
            @RequestParam Long customerId,
            @RequestParam Double amount,
            @RequestParam Payment.Method method,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String invoiceNumber) {
        Payment payment = invoicingService.recordPayment(customerId, amount, method, reference, invoiceNumber);
        return ResponseEntity.created(URI.create("/api/billing/payments/" + payment.getId())).body(payment);
    }

    @GetMapping("/customers/{customerId}/payments")
    public List<Payment> listPayments(@PathVariable Long customerId) {
        var customer = new com.irris.yamo_backend.entities.Customer();
        customer.setId(customerId);
        return paymentRepository.findByCustomer(customer);
    }
}
