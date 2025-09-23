package com.irris.yamo_backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String name; // e.g., "Home", "Work", "Parents' House"

    @Enumerated(EnumType.STRING)
    private District district;


    private String street;

    private String buildingNumber;
    private String apartmentNumber;
    private String additionalInstructions;

    private Double latitude;
    private Double longitude;

    private boolean isPrimary;

    public String getFullAddress() {
        StringBuilder addressBuilder = new StringBuilder();
        if (street != null) addressBuilder.append(street);
        if (buildingNumber != null) addressBuilder.append(", ").append(buildingNumber);
        if (district != null) addressBuilder.append(", ").append(district.getDisplayName());
        addressBuilder.append(", Douala, Cameroon");
        return addressBuilder.toString();
    }
}
