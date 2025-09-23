package com.irris.yamo_backend.services;

import com.irris.yamo_backend.dto.OrderItemRequest;
import com.irris.yamo_backend.dto.OrderRequest;
import com.irris.yamo_backend.dto.PickupRequest;
import com.irris.yamo_backend.dto.DeliveryRequest;
import com.irris.yamo_backend.entities.Order;
import com.irris.yamo_backend.entities.OrderItem;
import com.irris.yamo_backend.entities.Customer;
import com.irris.yamo_backend.entities.Promotion;
import com.irris.yamo_backend.entities.Pickup;
import com.irris.yamo_backend.entities.Delivery;
import com.irris.yamo_backend.repositories.CustomerRepository;
import com.irris.yamo_backend.repositories.OrderItemRepository;
import com.irris.yamo_backend.repositories.OrderRepository;
import com.irris.yamo_backend.repositories.PromotionRepository;
import com.irris.yamo_backend.repositories.PickupRepository;
import com.irris.yamo_backend.repositories.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final PromotionRepository promotionRepository;
    private final LoyaltyService loyaltyService;
    private final WhatsAppNotificationService waService;
    private final PickupRepository pickupRepository;
    private final DeliveryRepository deliveryRepository;

    public Order createOrder(OrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId()).orElseThrow();

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        double subtotal = 0.0;
        List<OrderItem> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (OrderItemRequest itemReq : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setItemType(itemReq.getItemType());
                item.setQuantity(itemReq.getQuantity());
                item.setPricePerUnit(itemReq.getPricePerUnit());
                item.setSpecialInstructions(itemReq.getSpecialInstructions());
                items.add(item);
                subtotal += itemReq.getPricePerUnit() * itemReq.getQuantity();
            }
        }
        order.setItems(items);

        double discount = 0.0;
        // Customer loyalty discount
        double loyaltyDiscountPct = loyaltyService.effectiveDiscountForCustomer(customer.getId(), request.getPromoCode());
        discount += subtotal * (loyaltyDiscountPct / 100.0);

        // Promo discount (adds to loyalty; consider capping later if needed)
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            var promoOpt = promotionRepository.findByCodeAndActiveTrue(request.getPromoCode());
            if (promoOpt.isPresent()) {
                Promotion promo = promoOpt.get();
                discount += subtotal * (promo.getDiscountPercentage() / 100.0);
            }
        }

        order.setDiscountAmount(discount);
        order.setTotalAmount(subtotal - discount);

        // Persist order with items via cascade or manually
        Order saved = orderRepository.save(order);
        for (OrderItem item : items) {
            item.setOrder(saved);
        }
        orderItemRepository.saveAll(items);
        // update customer stats/segment post-order
        loyaltyService.updateCustomerActivityAndSegment(customer.getId());

        try {
            String to = (customer.getWhatsappPhone() != null && !customer.getWhatsappPhone().isBlank())
                    ? customer.getWhatsappPhone()
                    : customer.getPhone();
            if (to != null && !to.isBlank()) {
                String msg = String.format("Commande #%d créée. Montant: %.0f XAF. Merci pour votre confiance.", saved.getId(), saved.getTotalAmount());
                waService.sendTextMessage(to, msg);
            }
        } catch (Exception ignored) { }

        return saved;
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        return orderRepository.findByCustomer(customer);
    }

    public Pickup schedulePickup(Long orderId, PickupRequest req) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        Pickup pickup = order.getPickup();
        if (pickup == null) pickup = new Pickup();
        pickup.setOrder(order);
        pickup.setContactName(req.getContactName());
        pickup.setContactPhone(req.getContactPhone());
        pickup.setAddress(req.getAddress());
        pickup.setScheduledDate(req.getScheduledDate());
        pickup.setStatus("SCHEDULED");
        Pickup saved = pickupRepository.save(pickup);
        order.setPickup(saved);
        order.setStatus("PICKUP_SCHEDULED");
        orderRepository.save(order);

        // Auto-plan delivery 3 business days after pickup (skip Sundays)
        Delivery delivery = order.getDelivery();
        if (delivery == null) delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setContactName(req.getContactName());
        delivery.setContactPhone(req.getContactPhone());
        delivery.setAddress(req.getAddress());
        // compute delivery date
        java.time.LocalDateTime deliveryDate = req.getScheduledDate();
        int added = 0;
        while (added < 3) {
            deliveryDate = deliveryDate.plusDays(1);
            if (deliveryDate.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
                added++;
            }
        }
        delivery.setScheduledDate(deliveryDate);
        delivery.setStatus("SCHEDULED");
        Delivery savedDelivery = deliveryRepository.save(delivery);
        order.setDelivery(savedDelivery);
        orderRepository.save(order);
        try {
            String to = order.getCustomer().getWhatsappPhone() != null && !order.getCustomer().getWhatsappPhone().isBlank()
                    ? order.getCustomer().getWhatsappPhone() : order.getCustomer().getPhone();
            if (to != null && !to.isBlank()) {
                String msg = String.format("Ramassage planifié le %s à %s.",
                        req.getScheduledDate(), req.getAddress());
                waService.sendTextMessage(to, msg);
            }
        } catch (Exception ignored) { }
        return saved;
    }

    public Delivery scheduleDelivery(Long orderId, DeliveryRequest req) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        Delivery delivery = order.getDelivery();
        if (delivery == null) delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setContactName(req.getContactName());
        delivery.setContactPhone(req.getContactPhone());
        delivery.setAddress(req.getAddress());
        delivery.setScheduledDate(req.getScheduledDate());
        delivery.setStatus("SCHEDULED");
        Delivery saved = deliveryRepository.save(delivery);
        order.setDelivery(saved);
        orderRepository.save(order);
        try {
            String to = order.getCustomer().getWhatsappPhone() != null && !order.getCustomer().getWhatsappPhone().isBlank()
                    ? order.getCustomer().getWhatsappPhone() : order.getCustomer().getPhone();
            if (to != null && !to.isBlank()) {
                String msg = String.format("Livraison planifiée le %s à %s.",
                        req.getScheduledDate(), req.getAddress());
                waService.sendTextMessage(to, msg);
            }
        } catch (Exception ignored) { }
        return saved;
    }

    public Order updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        try {
            String to = order.getCustomer().getWhatsappPhone() != null && !order.getCustomer().getWhatsappPhone().isBlank()
                    ? order.getCustomer().getWhatsappPhone() : order.getCustomer().getPhone();
            if (to != null && !to.isBlank()) {
                String msg = String.format("Statut de la commande #%d: %s", saved.getId(), status);
                waService.sendTextMessage(to, msg);
            }
        } catch (Exception ignored) { }
        return saved;
    }
}
