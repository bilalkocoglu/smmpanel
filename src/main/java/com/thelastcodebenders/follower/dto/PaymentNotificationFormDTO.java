package com.thelastcodebenders.follower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentNotificationFormDTO {
    private long bankAccountId;
    private String fullName;
    private double amount;
    private String date;
    private String time;
}
