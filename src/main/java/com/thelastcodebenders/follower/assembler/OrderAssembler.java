package com.thelastcodebenders.follower.assembler;

import com.thelastcodebenders.follower.dto.NewOrderFormDTO;
import com.thelastcodebenders.follower.dto.UserPageOrderDTO;
import com.thelastcodebenders.follower.enums.OrderStatusType;
import com.thelastcodebenders.follower.model.Order;
import com.thelastcodebenders.follower.model.Service;
import com.thelastcodebenders.follower.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderAssembler {
    private static final Logger log = LoggerFactory.getLogger(OrderAssembler.class);

    public Order convertFormDtoToOrder(NewOrderFormDTO newOrderFormDTO, User user, Service service){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String date = LocalDateTime.now().format(formatter);

        double apiPrice = newOrderFormDTO.getQuantity() * (service.getApiPrice() / 1000);
        double customPrice = newOrderFormDTO.getQuantity() * (service.getCustomPrice() / 1000);

        apiPrice = Double.parseDouble(String.format("%.2f", apiPrice));
        customPrice = Double.parseDouble(String.format("%.2f", customPrice));

        Order order = Order.builder()
                .date(date)
                .service(service)
                .user(user)
                .quantity(newOrderFormDTO.getQuantity())
                .destUrl(newOrderFormDTO.getUrl())
                .apiPrice(apiPrice)
                .customPrice(customPrice)
                .remain(0)
                .remainBalance(0)
                .closed(false)
                .status(OrderStatusType.PENDING)
                .startCount(0)
                .build();

        return order;
    }

    public List<UserPageOrderDTO> convertOrdersToUserTypeOrders(List<Order> orders){
        List<UserPageOrderDTO> userPageOrders = new ArrayList<>();
        for (Order order: orders) {
            UserPageOrderDTO userPageOrder = UserPageOrderDTO.builder()
                    .customPrice(order.getCustomPrice())
                    .date(order.getDate())
                    .destUrl(order.getDestUrl())
                    .quantity(order.getQuantity())
                    .remain(order.getRemain())
                    .remainBalance(order.getRemainBalance())
                    .serviceName(order.getService().getSubCategory().getCategory().getName() + ' ' + order.getService().getCustomName())
                    .startCount(order.getStartCount())
                    .status(order.getStatus())
                    .id(order.getId())
                    .build();

            userPageOrders.add(userPageOrder);
        }

        return userPageOrders;
    }
}
