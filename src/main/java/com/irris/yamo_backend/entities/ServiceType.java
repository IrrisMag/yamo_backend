package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "service_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique code to reference service (e.g., H1_WASH_DRY, H2_WASH_IRON)
    @Column(unique = true)
    private String code;

    private String name; // Display name (e.g., "Laver / sécher")

    @Enumerated(EnumType.STRING)
    private Mode mode; // PIECE or KG

    private Double price; // per piece if PIECE, per kg if KG

    private String description;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (active == null) active = true;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Mode {
        PIECE, KG
    }
}
