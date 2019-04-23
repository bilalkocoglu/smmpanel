package com.thelastcodebenders.follower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceFormDTO {
    @NotNull
    @NotEmpty
    private String customName;
    @NotNull
    private long subcategoryId;
    @NotNull
    private boolean active;
    @NotNull
    private double customPrice;
    @NotNull
    private int customMin;
    @NotNull
    private int customMax;
    @NotNull
    @NotEmpty
    private String description;
}
