package com.thelastcodebenders.follower.dto;

import com.thelastcodebenders.follower.enums.OrderStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitorPageOrderDTO {
    private long id;
    private String name;
    private int quantity;
    private String url;
    private OrderStatusType status;
}
