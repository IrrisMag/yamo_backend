package com.irris.yamo_backend.services;

import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.entities.Order;
import com.irris.yamo_backend.repositories.CustomerRepository;
import com.irris.yamo_backend.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoyaltyService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public void updateCustomerActivityAndSegment(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        List<Order> orders = orderRepository.findByCustomer(customer);
        if (orders.isEmpty()) {
            customer.setSegment(Customer.CustomerSegment.INACTIVE);
            customer.setDiscountPercentage(null);
            customer.setLastActivityAt(null);
            customerRepository.save(customer);
            return;
        }

        // Basic stats
        double totalAmount = orders.stream()
                .map(Order::getTotalAmount)
                .filter(a -> a != null)
                .mapToDouble(Double::doubleValue)
                .sum();
        LocalDateTime lastOrder = orders.stream()
                .map(Order::getCreatedAt)
                .filter(d -> d != null)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        customer.setLastActivityAt(lastOrder);

        // Frequency in last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long ordersLast30 = orders.stream()
                .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(thirtyDaysAgo))
                .count();

        // Segmentation rules (example):
        if (totalAmount >= 100000 || ordersLast30 >= 4) {
            customer.setSegment(Customer.CustomerSegment.VIP);
            customer.setDiscountPercentage(10.0);
        } else if (ordersLast30 >= 2 || totalAmount >= 50000) {
            customer.setSegment(Customer.CustomerSegment.REGULAR);
            customer.setDiscountPercentage(5.0);
        } else {
            // inactive if last order older than 60 days
            long daysSinceLast = ChronoUnit.DAYS.between(lastOrder, LocalDateTime.now());
            if (daysSinceLast > 60) {
                customer.setSegment(Customer.CustomerSegment.INACTIVE);
                customer.setDiscountPercentage(null);
            } else {
                customer.setSegment(null);
                customer.setDiscountPercentage(null);
            }
        }

        customerRepository.save(customer);
    }

    public double effectiveDiscountForCustomer(Long customerId, String promoCode) {
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        double discount = customer.getDiscountPercentage() == null ? 0.0 : customer.getDiscountPercentage();
        // Promo is applied separately by OrderService; here we could combine or cap total discount.
        return discount;
    }
}
