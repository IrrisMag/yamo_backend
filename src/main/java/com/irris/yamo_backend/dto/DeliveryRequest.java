package com.irris.yamo_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeliveryRequest {
    private String contactName;
    private String contactPhone;
    private String address;
    private LocalDateTime scheduledDate;
}
