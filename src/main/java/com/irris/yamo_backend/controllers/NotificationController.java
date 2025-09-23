package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.services.WhatsAppNotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final WhatsAppNotificationService waService;
    private final com.irris.yamo_backend.repositories.CustomerRepository customerRepository;
    private final com.irris.yamo_backend.repositories.OrderRepository orderRepository;
    private final com.irris.yamo_backend.repositories.InvoiceRepository invoiceRepository;

    @Value("${whatsapp.verify-token:}")
    private String verifyToken;

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping("/whatsapp/test")
    public ResponseEntity<?> sendWaTest(@RequestBody WhatsAppTestReq req) {
        Map<String, Object> resp = waService.sendTextMessage(req.getTo(), req.getText());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/whatsapp/webhook")
    public ResponseEntity<String> verifyWebhook(@RequestParam(name = "hub.mode", required = false) String mode,
                                                @RequestParam(name = "hub.verify_token", required = false) String token,
                                                @RequestParam(name = "hub.challenge", required = false) String challenge) {
        if (mode != null && token != null && challenge != null && token.equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("forbidden");
    }

    @PostMapping("/whatsapp/webhook")
    public ResponseEntity<?> receiveWebhook(@RequestBody Map<String, Object> payload,
                                            @RequestHeader Map<String, String> headers) {
        // Optionally verify X-Hub-Signature-256 in headers against your app secret
        try {
            log.info("WhatsApp webhook received: headers={}, payload={}", headers, payload);
        } catch (Exception ignored) { }
        return ResponseEntity.ok(Map.of("received", true));
    }

    // Campaign messaging to segments
    @PostMapping("/whatsapp/campaign")
    public ResponseEntity<?> sendCampaign(@RequestBody CampaignReq req) {
        // Build target set
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime since = req.getPeriodDays() != null ? now.minusDays(req.getPeriodDays()) : null;
        var customers = customerRepository.findAll();
        java.util.List<com.irris.yamo_backend.entities.Customer> targets = new java.util.ArrayList<>();
        for (var c : customers) {
            if (req.getSegment() != null && c.getSegment() != req.getSegment()) continue;
            // Inactivity filter
            if (req.getInactiveDays() != null && c.getLastActivityAt() != null) {
                if (!c.getLastActivityAt().isBefore(now.minusDays(req.getInactiveDays()))) continue;
            }
            // Amount and frequency
            double total = 0.0;
            long count = 0L;
            var orders = orderRepository.findByCustomer(c);
            for (var o : orders) {
                if (since == null || (o.getCreatedAt() != null && !o.getCreatedAt().isBefore(since))) {
                    total += (o.getTotalAmount() == null ? 0.0 : o.getTotalAmount());
                    count += 1;
                }
            }
            if (req.getMinAmount() != null && total < req.getMinAmount()) continue;
            if (req.getMinOrders() != null && count < req.getMinOrders()) continue;
            targets.add(c);
        }
        // Send messages
        java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();
        for (var c : targets) {
            String to = (c.getWhatsappPhone() != null && !c.getWhatsappPhone().isBlank()) ? c.getWhatsappPhone() : c.getPhone();
            if (to == null || to.isBlank()) continue;
            java.util.Map<String, Object> resp;
            if (req.getTemplateName() != null && !req.getTemplateName().isBlank()) {
                resp = waService.sendTemplateMessage(to, req.getTemplateName(), req.getTemplateLanguage() == null ? "fr" : req.getTemplateLanguage(), req.getTemplateParams());
            } else {
                String body = req.getText() == null ? "" : req.getText();
                resp = waService.sendTextMessage(to, body);
            }
            results.add(java.util.Map.of("customerId", c.getId(), "status", resp));
        }
        return ResponseEntity.ok(java.util.Map.of("targeted", targets.size(), "results", results));
    }

    @Data
    public static class WhatsAppTestReq {
        private String to;   // in E.164 format, e.g. 2376XXXXXXXX
        private String text; // message body
    }

    @Data
    public static class CampaignReq {
        private Double minAmount;   // montant min cumulé sur la période
        private Long minOrders;     // nb min de commandes sur la période
        private Integer periodDays; // période en jours pour statistiques
        private Integer inactiveDays; // clients inactifs depuis N jours
        private com.irris.yamo_backend.entities.Customer.CustomerSegment segment; // cible par segment

        private String text;              // message texte libre
        private String templateName;      // si utilisation d'un template WhatsApp
        private String templateLanguage;  // ex: fr
        private java.util.List<String> templateParams; // paramètres du template
    }
}
