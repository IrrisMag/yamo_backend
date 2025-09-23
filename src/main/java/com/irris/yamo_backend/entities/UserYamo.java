package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserYamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // email or phone

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Long customerId; // if CUSTOMER, link to Customer entity id
    private Long livreurId;  // if LIVREUR, link to Livreur entity id

    private Boolean active;

    @PrePersist
    public void prePersist() {
        if (active == null) active = true;
    }
}
