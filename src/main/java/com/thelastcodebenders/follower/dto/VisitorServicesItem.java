package com.thelastcodebenders.follower.dto;

import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.model.Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitorServicesItem {
    private Category category;
    private List<Service> services;
}
