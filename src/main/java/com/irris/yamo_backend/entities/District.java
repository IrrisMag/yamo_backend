package com.irris.yamo_backend.entities;

import lombok.Getter;

@Getter
public enum District {
    AKWA("Akwa"),
    BONAPRISO("Bonapriso"),
    BONANJO("Bonanjo"),
    DEIDO("Deido"),
    NEW_BELL("New Bell"),
    BASSA("Bassa"),
    MAKEPE("Makepe"),
    LOGBABA("Logbaba"),
    BEPANDA("Bépanda"),
    CITE_DES_PALMIERS("Cité des Palmiers"),
    YASSA("Yassa"),
    KOTTO("Kotto"),
    NKONGSAMBA("Nkongsamba"),
    BONABERI("Bonabéri"),
    NDOKOTI("Ndokoti"),
    PK_8("PK 8"),
    PK_12("PK 12"),
    PK_14("PK 14"),
    PK_16("PK 16"),
    PK_17("PK 17"),
    PK_18("PK 18"),
    PK_21("PK 21"),
    PK_24("PK 24");

    private final String displayName;

    District(String displayName) {
        this.displayName = displayName;
    }

    public static District fromDisplayName(String displayName) {
        for (District district : values()) {
            if (district.displayName.equalsIgnoreCase(displayName)) {
                return district;
            }
        }
        throw new IllegalArgumentException("No district with display name: " + displayName);
    }
}
