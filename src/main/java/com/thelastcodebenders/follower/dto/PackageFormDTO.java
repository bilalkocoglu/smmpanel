package com.thelastcodebenders.follower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackageFormDTO {
    private String serviceId;
    private String name;
    private int quantity;
    private double price;
    private String description;
}
