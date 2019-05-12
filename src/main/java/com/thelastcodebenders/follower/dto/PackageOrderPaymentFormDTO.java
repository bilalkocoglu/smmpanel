package com.thelastcodebenders.follower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackageOrderPaymentFormDTO {
    private String url;
    private String name;
    private String surname;
    private String email;
    private String number;
    private boolean checkbox;
}
