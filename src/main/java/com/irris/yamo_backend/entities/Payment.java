package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDateTime paymentDate;
    private Double amount; // raw amount received

    @Enumerated(EnumType.STRING)
    private Method method;

    private String reference; // MOMO/OM tx id, etc.

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice; // optional link to invoice if allocated

    public enum Method {
        OM, MOMO, ESPECES, VIREMENT
    }
}
