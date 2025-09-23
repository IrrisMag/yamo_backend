package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);
}
