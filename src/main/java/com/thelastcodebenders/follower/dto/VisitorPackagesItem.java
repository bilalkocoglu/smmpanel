package com.thelastcodebenders.follower.dto;

import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.model.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitorPackagesItem {
    private SubCategory subCategory;
    private boolean isFirst;
    private List<UserPagePackageDTO> packages;
}
