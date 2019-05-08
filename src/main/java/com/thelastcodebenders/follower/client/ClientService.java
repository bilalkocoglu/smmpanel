package com.thelastcodebenders.follower.client;

import com.thelastcodebenders.follower.assembler.ServiceAssembler;
import com.thelastcodebenders.follower.client.dto.*;
import com.thelastcodebenders.follower.enums.CreateAPIOrderType;
import com.thelastcodebenders.follower.model.API;
import com.thelastcodebenders.follower.model.Order;
import com.thelastcodebenders.follower.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@org.springframework.stereotype.Service
public class ClientService {
    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private RestTemplate restTemplate;
    private ServiceAssembler serviceAssembler;

    public ClientService(RestTemplate restTemplate,
                         ServiceAssembler serviceAssembler){
        this.restTemplate = restTemplate;
        this.serviceAssembler = serviceAssembler;
    }

    public double getBalance(API api){
        try {
            GeneralRequest generalRequest = GeneralRequest
                    .builder()
                    .key(api.getSecretKey())
                    .action("balance")
                    .build();
            ResponseEntity<BalanceResponse> response = restTemplate.postForEntity( api.getUrl(), generalRequest, BalanceResponse.class );

            if (response.getStatusCode() != HttpStatus.OK){
                log.error("Client Service Balance Method Error");
                return -1;
            }else {
                return Double.valueOf(response.getBody().getBalance());
            }
        }catch (Exception e){
            log.error("Client Service Balance Method Error -> " + e.getMessage());
            return -1;
        }
    }

    public List<Service> getAllServices(API api){
        try{
            GeneralRequest generalRequest = GeneralRequest
                    .builder()
                    .key(api.getSecretKey())
                    .action("services")
                    .build();
            ResponseEntity<ServiceClientDTO[]> response = restTemplate.postForEntity(api.getUrl(), generalRequest, ServiceClientDTO[].class);

            if (response.getStatusCode() != HttpStatus.OK){
                log.error("Client Service getAllServices Method Error -> Response Code : " + response.getStatusCodeValue());
                return null;
            }

            List<ServiceClientDTO> serviceClientDTOList = Arrays.asList(response.getBody());
            List<Service> services = new ArrayList<>();
            for (ServiceClientDTO serviceClientDTO : serviceClientDTOList) {
                services.add(serviceAssembler.convertClientDtoToService(serviceClientDTO, api));
            }

            return services;
        }catch (Exception e){
            log.error("Client Service getAllServices Error -> " + e.getMessage());
            return null;
        }
    }

    public String createOrderReturnOrderId(Order order, CreateAPIOrderType orderType){
        try {
            CreateOrderRequest createOrderRequest = null;
            String url = null;

            if (orderType == CreateAPIOrderType.SERVICE){
                createOrderRequest = CreateOrderRequest.builder()
                        .key(order.getService().getApi().getSecretKey())
                        .action("add")
                        .service(order.getService().getApiServiceId())
                        .link(order.getDestUrl())
                        .quantity(String.valueOf(order.getQuantity()))
                        .build();
                url = order.getService().getApi().getUrl();
            }else if(orderType == CreateAPIOrderType.PACKAGE){
                createOrderRequest = CreateOrderRequest.builder()
                        .key(order.getPackagee().getService().getApi().getSecretKey())
                        .action("add")
                        .service(order.getPackagee().getService().getApiServiceId())
                        .link(order.getDestUrl())
                        .quantity(String.valueOf(order.getPackagee().getQuantity()))
                        .build();
                url = order.getPackagee().getService().getApi().getUrl();
            }

            ResponseEntity<CreateOrderResponse> response = restTemplate.postForEntity(url, createOrderRequest, CreateOrderResponse.class);

            if (response.getStatusCode() != HttpStatus.OK){
                log.error("Client Service createOrderReturnOrderId Method Error -> Response Code : " + response.getStatusCodeValue());
                return null;
            }

            return String.valueOf(response.getBody().getOrder());
        }catch (Exception e){
            log.error("Client Service Return Order Id Error -> " + e.getMessage());
            return null;
        }
    }

    public String createDrawPrizeOrderReturnOrderId(Service service, String link, String quantity){
        try {
            CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                    .key(service.getApi().getSecretKey())
                    .action("add")
                    .service(service.getApiServiceId())
                    .link(link)
                    .quantity(quantity)
                    .build();
            String url = service.getApi().getUrl();
            ResponseEntity<CreateOrderResponse> response = restTemplate.postForEntity(url, createOrderRequest, CreateOrderResponse.class);

            if (response.getStatusCode() != HttpStatus.OK){
                log.error("Client Service createOrderReturnOrderId Method Error -> Response Code : " + response.getStatusCodeValue());
                return null;
            }

            return String.valueOf(response.getBody().getOrder());
        }catch (Exception e){
            log.error("Client Service Create Draw Prize Return Order Id Error -> " + e.getMessage());
            return null;
        }
    }

    public OrderStatusResponse orderStatus(String apiOrderId, API api){
        OrderStatusRequest req = OrderStatusRequest.builder()
                .action("status")
                .key(api.getSecretKey())
                .order(apiOrderId)
                .build();
        String url = api.getUrl();

        ResponseEntity<OrderStatusResponse> res = restTemplate.postForEntity(url, req, OrderStatusResponse.class);

        if (res.getStatusCode() != HttpStatus.OK){
            log.error("Client Service Order Status Error -> Response Code : " + res.getStatusCodeValue());
            return null;
        }

        return res.getBody();
    }
}
