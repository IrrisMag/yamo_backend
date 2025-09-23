package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TaskType type; // PICKUP, DELIVERY, OTHER

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order; // optional link to order

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer; // optional link to customer

    private String title; // e.g. "Ramassage client X"
    private String description;

    private String addressLine; // human readable address
    private Double latitude;
    private Double longitude;

    private LocalDateTime scheduledAt;
    private Integer remindBeforeMinutes; // e.g., 30

    @ManyToOne
    @JoinColumn(name = "livreur_id")
    private Livreur assigneeLivreur; // optional

    @ElementCollection
    @CollectionTable(name = "task_participants", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "phone")
    @Builder.Default
    private List<String> participantsPhones = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private TaskStatus status; // SCHEDULED, IN_PROGRESS, DONE, CANCELLED, MISSED

    private String notes;
    private String proofPhotoUrl; // optional proof
    private String proofSignatureUrl; // optional proof

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) status = TaskStatus.SCHEDULED;
        if (remindBeforeMinutes == null) remindBeforeMinutes = 30;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TaskType {
        PICKUP, DELIVERY, OTHER
    }

    public enum TaskStatus {
        SCHEDULED, IN_PROGRESS, DONE, CANCELLED, MISSED
    }
}
