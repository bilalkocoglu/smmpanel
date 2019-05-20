package com.thelastcodebenders.follower.assembler;

import com.thelastcodebenders.follower.client.panel.dto.ServiceClientDTO;
import com.thelastcodebenders.follower.dto.ServiceFormDTO;
import com.thelastcodebenders.follower.dto.UserPageServiceDTO;
import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.model.API;
import com.thelastcodebenders.follower.model.Service;
import com.thelastcodebenders.follower.model.SubCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class ServiceAssembler {
    private static final Logger log = LoggerFactory.getLogger(ServiceAssembler.class);

    public Service convertClientDtoToService(ServiceClientDTO serviceClientDTO, API api){
        Service service = new Service();
        service.setState(ServiceState.PASSIVE);
        service.setApi(api);
        service.setApiServiceId(serviceClientDTO.getService());
        service.setApiName(serviceClientDTO.getName());

        if (api.isUseUSD()){
            service.setApiUSDPrice(Double.valueOf(serviceClientDTO.getRate()));

            double apiPriceTL = service.getApiUSDPrice() * api.getRateUSD();
            service.setApiPrice(Double.valueOf(new DecimalFormat("##.##").format(apiPriceTL)));
        }else {
            service.setApiPrice(Double.valueOf(serviceClientDTO.getRate()));
        }
        service.setApiMinPiece(Integer.valueOf(serviceClientDTO.getMin()));
        service.setApiMaxPiece(Integer.valueOf(serviceClientDTO.getMax()));
        service.setApiDripfeed(serviceClientDTO.isDripfeed());
        service.setApiCategory(serviceClientDTO.getCategory());

        return service;
    }

    public ServiceFormDTO convertServiceToFormDto(Service service){
        boolean active = false;
        if (service.getState() == ServiceState.ACTIVE){
            active = true;
        }
        return ServiceFormDTO.builder()
                .active(active)
                .customMax(service.getCustomMaxPiece())
                .customMin(service.getCustomMinPiece())
                .customName(service.getCustomName())
                .customPrice(service.getCustomPrice())
                .description(service.getDescription())
                .build();
    }

    public Service convertFormDtoToService(ServiceFormDTO serviceFormDTO, Service service, SubCategory subCategory){
        ServiceState state = null;
        if (serviceFormDTO.isActive()){
            state = ServiceState.ACTIVE;
        }else{
            state = ServiceState.PASSIVE;
        }
        service.setCustomName(serviceFormDTO.getCustomName());
        service.setSubCategory(subCategory);
        service.setState(state);
        service.setCustomPrice(serviceFormDTO.getCustomPrice());
        service.setCustomMinPiece(serviceFormDTO.getCustomMin());
        service.setCustomMaxPiece(serviceFormDTO.getCustomMax());
        service.setDescription(serviceFormDTO.getDescription());
        return service;
    }

    public UserPageServiceDTO convertServiceToUserPageService(Service service){
        return UserPageServiceDTO.builder()
                .customMaxPiece(service.getCustomMaxPiece())
                .customMinPiece(service.getCustomMinPiece())
                .customName(service.getCustomName())
                .customPrice(service.getCustomPrice())
                .description(service.getDescription())
                .id(service.getId())
                .subCategory(service.getSubCategory())
                .build();
    }
}
