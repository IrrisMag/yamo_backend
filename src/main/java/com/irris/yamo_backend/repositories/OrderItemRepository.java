package com.irris.yamo_backend.repositories;

import com.irris.yamo_backend.entities.Order;
import com.irris.yamo_backend.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
}
