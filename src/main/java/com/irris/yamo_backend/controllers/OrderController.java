package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.dto.OrderRequest;
import com.irris.yamo_backend.dto.PickupRequest;
import com.irris.yamo_backend.dto.DeliveryRequest;
import com.irris.yamo_backend.dto.StatusUpdateRequest;
import com.irris.yamo_backend.entities.Order;
import com.irris.yamo_backend.entities.Pickup;
import com.irris.yamo_backend.entities.Delivery;
import com.irris.yamo_backend.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody OrderRequest request) {
        Order saved = orderService.createOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        if (order == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public List<Order> byCustomer(@RequestParam("customerId") Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    @PostMapping("/{id}/pickup")
    public ResponseEntity<Pickup> schedulePickup(@PathVariable Long id, @RequestBody PickupRequest request) {
        Pickup saved = orderService.schedulePickup(id, request);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/delivery")
    public ResponseEntity<Delivery> scheduleDelivery(@PathVariable Long id, @RequestBody DeliveryRequest request) {
        Delivery saved = orderService.scheduleDelivery(id, request);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        Order saved = orderService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(saved);
    }
}
