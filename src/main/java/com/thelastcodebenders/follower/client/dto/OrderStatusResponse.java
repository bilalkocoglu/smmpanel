package com.thelastcodebenders.follower.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatusResponse {
    private String charge;      //tutar
    private String start_count; //başlangıç sayısı
    private String status;      //durum
    private String remains;     //kalan
    private String currency;    //para birimi
}
