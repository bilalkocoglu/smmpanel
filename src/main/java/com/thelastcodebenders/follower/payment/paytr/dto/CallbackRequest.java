package com.thelastcodebenders.follower.payment.paytr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallbackRequest {
    private String merchant_oid;
    private String status;
    private String total_amount;
    private String hash;
    private String failed_reason_code;
    private String failed_reason_msg;
    private String test_mode;
    private String payment_type;
    private String currency;
    private String payment_amount;
}
