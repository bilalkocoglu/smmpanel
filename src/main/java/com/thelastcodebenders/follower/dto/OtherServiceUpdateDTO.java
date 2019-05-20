package com.thelastcodebenders.follower.dto;

import com.thelastcodebenders.follower.model.Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtherServiceUpdateDTO {
    private List<Service> deletedService = new ArrayList<>();
    private List<Service> newService = new ArrayList<>();
    private Map<Service, Service> equivalentMap = new HashMap<>();
}
