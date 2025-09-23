package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Builder
@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String surname;
    private String phone;
    private String code;

    private String whatsappPhone;
    private String email;
    private String address1;
    private String address2;
    private String nui; // Numéro contribuable
    private String rccm; // Registre du Commerce
    private String companyName; // Raison sociale si entreprise

    @Enumerated(EnumType.STRING)
    private CustomerSegment segment; // VIP, REGULAR, INACTIVE, ENTERPRISE, DEPOSIT

    private Double discountPercentage; // remise automatique/manuelle

    // Crédit disponible (surplus de paiements à imputer sur prochaines factures)
    private Double creditBalance; // peut être null -> traiter comme 0.0

    private LocalDateTime lastActivityAt; // pour statistiques de fidélité

    @ElementCollection
    @CollectionTable(name = "customer_tags", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "tags")
    private List<String> tags; // segmentation libre (ex: fidelite_mensuelle, >10000, ...)

    @JsonManagedReference("customer-orders")
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    public enum CustomerSegment {
        VIP, REGULAR, INACTIVE, ENTERPRISE, DEPOSIT
    }
}
