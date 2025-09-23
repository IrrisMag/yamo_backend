package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "livreur_locations", indexes = {
        @Index(name = "idx_livreur_loc_livreur_ts", columnList = "livreur_id,capturedAt"),
        @Index(name = "idx_livreur_loc_order_ts", columnList = "order_id,capturedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LivreurLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livreur_id")
    private Livreur livreur;

    @ManyToOne(optional = true)
    @JoinColumn(name = "order_id")
    private Order order; // optional: active order being serviced

    private Double latitude;
    private Double longitude;
    private Double heading; // degrees, optional
    private Double speed;   // m/s, optional

    private LocalDateTime capturedAt;

    @PrePersist
    public void prePersist() {
        if (capturedAt == null) capturedAt = LocalDateTime.now();
    }
}
