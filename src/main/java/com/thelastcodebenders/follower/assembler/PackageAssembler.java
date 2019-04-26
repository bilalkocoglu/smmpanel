package com.thelastcodebenders.follower.assembler;

import com.thelastcodebenders.follower.dto.PackageFormDTO;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PackageAssembler {
    private static final Logger log = LoggerFactory.getLogger(PackageAssembler.class);

    public Package convertFormDtoToPackage(PackageFormDTO packageFormDTO, Service service){
        return Package.builder()
                .description(packageFormDTO.getDescription())
                .name(packageFormDTO.getName())
                .price(packageFormDTO.getPrice())
                .quantity(packageFormDTO.getQuantity())
                .category(service.getSubCategory().getCategory())
                .service(service)
                .state(true)
                .build();
    }
}
