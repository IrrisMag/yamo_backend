package com.irris.yamo_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {
    private Long customerId;
    private List<OrderItemRequest> items;
    private String promoCode;
}

