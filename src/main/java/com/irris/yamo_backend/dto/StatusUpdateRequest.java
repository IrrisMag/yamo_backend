package com.irris.yamo_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequest {
    private String status; // PENDING, PICKUP_SCHEDULED, IN_PROCESS, READY, OUT_FOR_DELIVERY, DELIVERED, COMPLETED, CANCELED
}
