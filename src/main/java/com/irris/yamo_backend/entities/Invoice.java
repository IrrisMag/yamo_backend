package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g., FAC-000001 (autonumber set in service when creating)
    @Column(unique = true)
    private String invoiceNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Assuming one invoice per order
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private LocalDateTime issueDate;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private Status status; // DRAFT, ISSUED, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED

    private Double totalAmount;   // total to be paid
    private Double paidAmount;    // sum of allocations
    private Double balanceAmount; // total - paid

    public enum Status {
        DRAFT,
        ISSUED,
        PARTIALLY_PAID,
        PAID,
        OVERDUE,
        CANCELLED
    }
}
