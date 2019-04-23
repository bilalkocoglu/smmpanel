package com.thelastcodebenders.follower.service;


import com.thelastcodebenders.follower.assembler.OrderAssembler;
import com.thelastcodebenders.follower.client.ClientService;
import com.thelastcodebenders.follower.client.dto.OrderStatusResponse;
import com.thelastcodebenders.follower.dto.NewOrderFormDTO;
import com.thelastcodebenders.follower.enums.OrderStatusType;
import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.model.Order;
import com.thelastcodebenders.follower.model.Service;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import javax.security.auth.login.LoginException;
import java.util.List;

@org.springframework.stereotype.Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private ServiceService serviceService;
    private UserService userService;
    private OrderRepository orderRepository;
    private OrderAssembler orderAssembler;
    private ClientService clientService;
    private ApiService apiService;

    public OrderService(ServiceService serviceService,
                        UserService userService,
                        OrderRepository orderRepository,
                        OrderAssembler orderAssembler,
                        ClientService clientService,
                        ApiService apiService){
        this.serviceService = serviceService;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderAssembler = orderAssembler;
        this.clientService = clientService;
        this.apiService = apiService;
    }

    //net kazanç
    public double getWinnings(){
        List<Order> completedOrders = orderRepository.findByClosed(true);

        double possitive = 0;
        double negative = 0;

        for (Order order: completedOrders) {
            if (order.getStatus() == OrderStatusType.COMPLETED){
                possitive += order.getCustomPrice();
                negative += order.getApiPrice();
            }else if (order.getStatus() == OrderStatusType.PARTIAL){
                possitive += order.getCustomPrice() - order.getRemainBalance();

                //api tarafından bize yapılması beklenen iade
                double apiRemain = (order.getRemainBalance() * order.getApiPrice()) / order.getCustomPrice();
                negative += order.getApiPrice() - apiRemain;
            }
        }

        double total = possitive - negative;

        return Double.parseDouble(String.format("%.2f", total));
    }

    public boolean isAlreadyUrl(String url){
        List<Order> activeOrders = orderRepository.findByClosed(false);

        for (Order order: activeOrders) {
            if (order.getDestUrl().equals(url))
                return true;
        }

        return false;
    }

    public boolean createOrder(NewOrderFormDTO newOrderForm, long serviceId) throws LoginException {
        try {
            boolean isAlreadyUrl = isAlreadyUrl(newOrderForm.getUrl());

            if (isAlreadyUrl){
                log.error("Url üzerinde aktif bir sipariş mevcut.");
                throw new RuntimeException("Sipariş vermek istediğiniz link için tamamlanmamış bir sipariş mevcut. Lütfen tamamlandıktan sonra bu işlemi tekrarlayınız.");
            }

            Service service = serviceService.findServiceById(serviceId);

            if (service == null){
                throw new RuntimeException("Sipariş vermek istediğiniz servis bulunamadı !");
            }else if (service.getState() != ServiceState.ACTIVE){
                throw new RuntimeException("Şu anda seçtiğiniz servis için sipariş alamıyoruz. Daha sonra tekrar deneyin.");
            }else {
                if (newOrderForm.getQuantity()>service.getCustomMaxPiece() ||
                        newOrderForm.getQuantity()<service.getCustomMinPiece()){
                    throw new RuntimeException("Miktar ilgili servis için belirlenen aralığın dışında !");
                }
            }

            User user = userService.getAuthUser();

            Order order = orderAssembler.convertFormDtoToOrder(newOrderForm, user, service);

            if (user.getBalance() < order.getCustomPrice()){
                log.error("Order Service Create Order Error -> Insufficient Balance !");
                throw new RuntimeException("Sipariş için yeterli bakiyeye sahip değilsiniz !");
            }

            if( order.getApiPrice() > service.getApi().getBalance() ){
                log.error("Order Service Create Order Error -> API Insufficient Balance !");
                throw new RuntimeException("İşleminiz gerçekleştirilemedi. " +
                        "Bir destek talebi açıp sistem yöneticisine sipariş hakkında bilgi verip yardım isteyebilirsiniz.");
            }

            String apiOrderId = clientService.createOrderReturnOrderId(order);

            if (apiOrderId == null){
                throw new RuntimeException("Şu anda seçtiğiniz servis için sipariş alamıyoruz. Daha sonra tekrar deneyin.");
            }

            order.setApiOrderId(apiOrderId);

            order = orderRepository.save(order);

            if (order != null){
                double newBalance = user.getBalance() - order.getCustomPrice();
                userService.adminBasedUpdate(user.getId(), user, String.valueOf(newBalance));

                apiService.asyncApiUpdateBalance(order.getService().getApi());
            }else {
                log.error("Line 71 Save Error !");
            }

            return true;
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("Order Service Create Order Error -> " + e.getMessage());
            return false;
        }
    }

    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByAuthUser() throws LoginException {
        User user = userService.getAuthUser();

        return orderRepository.findByUser(user, new Sort(Sort.Direction.DESC, "id"));
    }

    public List<Order> getOrdersByUser(User user){
        return orderRepository.findByUser(user, new Sort(Sort.Direction.DESC, "id"));
    }

    public int getAuthUserActiveOrderCount() throws LoginException {
        User user = userService.getAuthUser();

        return orderRepository.countByUserAndClosed(user, false);
    }

    //shedule task method !!
    public void updateActiveOrderStatus(){
        try {
            List<Order> activeOrders = orderRepository.findByClosed(false);

            for (Order order: activeOrders) {
                OrderStatusResponse orderStatusResponse = clientService.orderStatus(order.getApiOrderId(), order.getService().getApi());

                if (orderStatusResponse.getStatus().equals("Pending")){
                    //sipariş alındı
                    log.info(order.getId() + " -> Pending !");

                    try {
                        if ( orderStatusResponse.getStart_count() != null){
                            order.setStartCount(Integer.valueOf(orderStatusResponse.getStart_count()));
                        }
                    }catch (Exception e){
                        log.error("Order Service OrderStatus Update Error -> Start Count Not Convert Integer !");
                    }

                    order.setStatus(OrderStatusType.PENDING);
                }
                else if (orderStatusResponse.getStatus().equals("Inprogress")){
                    //yükleniyor
                    log.info(order.getId() + " -> Inprogress !");


                    try {
                        if ( orderStatusResponse.getStart_count() != null){
                            order.setStartCount(Integer.valueOf(orderStatusResponse.getStart_count()));
                        }
                    }catch (Exception e){
                        log.error("Order Service OrderStatus Update Error -> Start Count Not Convert Integer !");
                    }

                    order.setStatus(OrderStatusType.INPROGRESS);
                }
                else if (orderStatusResponse.getStatus().equals("Completed")){
                    //tamamlandı
                    log.info(order.getId() + " -> Complated !");


                    try {
                        if ( orderStatusResponse.getStart_count() != null){
                            order.setStartCount(Integer.valueOf(orderStatusResponse.getStart_count()));
                        }
                    }catch (Exception e){
                        log.error("Order Service OrderStatus Update Error -> Start Count Not Convert Integer !");
                    }



                    order.setClosed(true);
                    order.setStatus(OrderStatusType.COMPLETED);
                }
                else if (orderStatusResponse.getStatus().equals("Partial")){
                    //bir kısmı tamamlandı kalanı iade edildi
                    log.info(order.getId() + " -> Partial !");


                    int remain = Integer.valueOf(orderStatusResponse.getRemains());
                    double remainBalance = (order.getCustomPrice() * remain) / order.getQuantity();

                    order.setRemain(remain);
                    order.setRemainBalance(remainBalance);
                    //kullanıcıya para iadesi
                    double newBalance = order.getUser().getBalance() + remainBalance;
                    userService.adminBasedUpdate(order.getUser().getId(), order.getUser(), String.valueOf(newBalance));

                    try {
                        if ( orderStatusResponse.getStart_count() != null){
                            order.setStartCount(Integer.valueOf(orderStatusResponse.getStart_count()));
                        }
                    }catch (Exception e){
                        log.error("Order Service OrderStatus Update Error -> Start Count Not Convert Integer !");
                    }
                    //bakiye güncellemesi
                    apiService.asyncApiUpdateBalance(order.getService().getApi());



                    order.setClosed(true);
                    order.setStatus(OrderStatusType.PARTIAL);
                }
                else if (orderStatusResponse.getStatus().equals("Processing")){
                    //gönderim sırasında
                    log.info(order.getId() + " -> Processing !");


                    try {
                        if ( orderStatusResponse.getStart_count() != null){
                            order.setStartCount(Integer.valueOf(orderStatusResponse.getStart_count()));
                        }
                    }catch (Exception e){
                        log.error("Order Service OrderStatus Update Error -> Start Count Not Convert Integer !");
                    }

                    order.setStatus(OrderStatusType.PROCESSING);
                }
                else if (orderStatusResponse.getStatus().equals("Canceled")){
                    //iptal edildi
                    //para iade edilecek, api bakiyesi güncellenecek
                    log.info(order.getId() + " -> Canceled !");


                    //kullanıcı para iadesi
                    double newBalance = order.getUser().getBalance() + order.getCustomPrice();
                    userService.adminBasedUpdate(order.getUser().getId(), order.getUser(), String.valueOf(newBalance));


                    //bakiye güncellemesi
                    apiService.asyncApiUpdateBalance(order.getService().getApi());

                    try {
                        if ( orderStatusResponse.getStart_count() != null){
                            order.setStartCount(Integer.valueOf(orderStatusResponse.getStart_count()));
                        }
                    }catch (Exception e){
                        log.error("Order Service OrderStatus Update Error -> Start Count Not Convert Integer !");
                    }



                    order.setClosed(true);
                    order.setStatus(OrderStatusType.CANCELED);
                }

                orderRepository.save(order);
            }
        }catch (Exception e){
            log.error("Order Service Order Status Update Error -> " + e.getMessage());
        }
    }
}
