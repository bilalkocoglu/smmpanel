package com.thelastcodebenders.follower.dto.userservices;

import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.model.Package;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserServicesListItem {
    private Category category;
    private boolean first;
    private List<UserServicesListSubItem> subItems;
    private List<Package> packages;
}
