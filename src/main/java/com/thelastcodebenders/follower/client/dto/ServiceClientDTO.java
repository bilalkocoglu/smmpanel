package com.thelastcodebenders.follower.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceClientDTO {
    private String service;
    private String name;
    private String type;
    private String rate;
    private String min;
    private String max;
    private boolean dripfeed;
    private String category;
}
