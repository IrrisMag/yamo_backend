package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.*;


@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Livreur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identity
    private String name;        // NOM
    private String surname;     // PRENOM
    private String nationalId;  // N CNI

    // Contact
    private String phone;       // TEL
    private String email;       // COURRIEL

    // Vehicle
    private String vehicleType;        // TYPE DE VEHICULE
    private String vehicleBrand;       // MARQUE
    private String vehicleColor;       // COULEUR
    private String vehicleRegistration; // MATRICULE/CHASSIS

    // Profile
    private String photoUrl;    // PHOTO
    private String comment;     // COMMENTAIRE

    private Boolean active;
}
