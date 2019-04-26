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
public class UserPageOrderDTO {
    private long id;
    private String date;
    private String destUrl;
    private double customPrice;
    private String serviceName;
    private int quantity;
    private int startCount;
    private OrderStatusType status;
    private int remain;
    private double remainBalance;
}
