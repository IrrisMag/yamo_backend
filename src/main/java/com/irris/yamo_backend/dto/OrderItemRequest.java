package com.irris.yamo_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    private String itemType;
    private Integer quantity;
    private Double pricePerUnit;
    private String specialInstructions;
}
