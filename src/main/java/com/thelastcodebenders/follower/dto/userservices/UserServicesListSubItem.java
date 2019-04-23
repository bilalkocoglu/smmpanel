package com.thelastcodebenders.follower.dto.userservices;

import com.thelastcodebenders.follower.model.Service;
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
public class UserServicesListSubItem {
    private SubCategory subCategory;
    private List<Service> services;
}
