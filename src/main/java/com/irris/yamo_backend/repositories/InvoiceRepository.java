package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findTopByOrderByIdDesc();
    List<Invoice> findByCustomer(Customer customer);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
