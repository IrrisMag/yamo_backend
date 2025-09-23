package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String contactName;
    private String contactPhone;
    private String address; // City in Douala

    @ManyToOne
    @JoinColumn(name = "livreur_id")
    private Livreur livreur;

    private LocalDateTime scheduledDate;
    private LocalDateTime actualDate;
    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    // Getters and setters
}
