package com.thelastcodebenders.follower.dto;

import com.thelastcodebenders.follower.model.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPageServiceDTO {
    private long id;
    private int customMaxPiece;
    private int customMinPiece;
    private String customName;
    private double customPrice;     //satis fiyati
    private String description;
    private SubCategory subCategory;
}
