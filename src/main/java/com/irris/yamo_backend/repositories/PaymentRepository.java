package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.entities.Invoice;
import com.irris.yamo_backend.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomer(Customer customer);
    List<Payment> findByInvoice(Invoice invoice);
}
