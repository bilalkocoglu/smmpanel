package com.thelastcodebenders.follower.client.panel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderRequest {
    private String key;
    private String action;
    private String service;
    private String link;
    private String quantity;
}
