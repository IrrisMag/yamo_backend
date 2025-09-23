package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private Category category; // WATER, RENT, SOAP, SUPPLIES, ENERGY, WAGES, TRANSPORT, OTHER

    private String reference; // invoice ref, receipt id
    private String notes;
    private String proofUrl; // attachment (image/pdf)

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public enum Category {
        WATER, RENT, SOAP, SUPPLIES, ENERGY, WAGES, TRANSPORT, OTHER
    }
}
