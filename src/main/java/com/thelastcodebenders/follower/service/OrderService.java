package com.thelastcodebenders.follower.service;


import com.thelastcodebenders.follower.assembler.OrderAssembler;
import com.thelastcodebenders.follower.client.panel.PanelService;
import com.thelastcodebenders.follower.client.panel.dto.OrderStatusResponse;
import com.thelastcodebenders.follower.dto.NewOrderFormDTO;
import com.thelastcodebenders.follower.dto.UserPageOrderDTO;
import com.thelastcodebenders.follower.dto.VisitorPageOrderDTO;
import com.thelastcodebenders.follower.enums.CreateAPIOrderType;
import com.thelastcodebenders.follower.enums.OrderStatusType;
import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.Order;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.model.Service;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private ServiceService serviceService;
    private UserService userService;
    private OrderRepository orderRepository;
    private OrderAssembler orderAssembler;
    private PanelService panelService;
    private ApiService apiService;
    private PackageService packageService;

    public OrderService(ServiceService serviceService,
                        UserService userService,
                        OrderRepository orderRepository,
                        OrderAssembler orderAssembler,
                        PanelService panelService,
                        ApiService apiService,
                        PackageService packageService){
        this.serviceService = serviceService;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderAssembler = orderAssembler;
        this.panelService = panelService;
        this.apiService = apiService;
        this.packageService = packageService;
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

    public boolean createPackageOrder(NewOrderFormDTO newOrderFrom, long packageId) throws LoginException{
        try {
            if (isNullOrEmpty(newOrderFrom.getUrl()))
                throw new DetectedException("Tüm alanları doğru girmelisiniz !");
            else if(newOrderFrom.getUrl().length()>400)
                throw new DetectedException("Tüm alanları doğru girmelisiniz !");

            boolean isAlreadyUrl = isAlreadyUrl(newOrderFrom.getUrl());

            if (isAlreadyUrl){
                log.error("Url üzerinde aktif sipariş mevcut !");
                throw new DetectedException("Sipariş vermek istediğiniz link için tamamlanmamış sipariş mevcut. Lütfen tamamlandıktan sonra bu işlemi tekrarlayınız.");
            }

            Package pkg = packageService.findById(packageId);

            if (pkg == null){
                throw new DetectedException("Sipariş vermek istediğiniz paket bulunamadı !");
            }else if (!pkg.isState()){
                throw new DetectedException("Şu anda seçtiğiniz paket için sipariş alamıyoruz. Daha sonra tekrar deneyin.");
            }

            User user = userService.getAuthUser();

            Order order = orderAssembler.convertFormDTOToPackageOrder(newOrderFrom, user, pkg);

            if (user.getBalance() < order.getCustomPrice()){
                log.error("OrderService createPackageOrder Error -> Insufficient Balance !");
                throw new DetectedException("Sipariş için yeterli bakiyeye sahip değilsiniz !");
            }

            if( order.getApiPrice() > pkg.getService().getApi().getBalance() ){
                log.error("OrderService createPackageOrder Error -> API Insufficient Balance !");
                throw new DetectedException("İşleminiz gerçekleştirilemedi. " +
                        "Bir destek talebi açıp sistem yöneticisine sipariş hakkında bilgi verip yardım isteyebilirsiniz.");
            }

            String apiOrderId = panelService.createOrderReturnOrderId(order, CreateAPIOrderType.PACKAGE);

            if (apiOrderId == null){
                log.error("OrderService createPackageOrder Error -> API Order Id Null !");
                throw new DetectedException("Şu anda seçtiğiniz servis için sipariş alamıyoruz. Daha sonra tekrar deneyin.");
            }

            order.setApiOrderId(apiOrderId);

            order = orderRepository.save(order);

            if (order != null){
                double newBalance = user.getBalance() - order.getCustomPrice();
                userService.adminBasedUpdate(user.getId(), user, String.valueOf(newBalance));

                apiService.asyncApiUpdateBalance(order.getService().getApi());
            }else {
                log.error("OrderService createPackageOrder Order Error -> order not save");
            }

            return true;
        }catch (Exception e){
            if (e instanceof DetectedException)
                throw e;
            log.error("OrderService createPackageOrder Order Error -> " + e.getMessage());
            return false;
        }
    }

    public boolean createServiceOrder(NewOrderFormDTO newOrderForm, long serviceId) throws LoginException {
        try {
            if (!newOrderFormValidate(newOrderForm)){
                throw new DetectedException("Tüm alanları doğru girmelisiniz !");
            }

            boolean isAlreadyUrl = isAlreadyUrl(newOrderForm.getUrl());

            if (isAlreadyUrl){
                log.error("Url üzerinde aktif bir sipariş mevcut.");
                throw new DetectedException("Sipariş vermek istediğiniz link için tamamlanmamış sipariş mevcut. Lütfen tamamlandıktan sonra bu işlemi tekrarlayınız.");
            }

            Service service = serviceService.findServiceById(serviceId);

            if (service == null){
                throw new DetectedException("Sipariş vermek istediğiniz servis bulunamadı !");
            }else if (service.getState() != ServiceState.ACTIVE){
                throw new DetectedException("Şu anda seçtiğiniz servis için sipariş alamıyoruz. Daha sonra tekrar deneyin.");
            }else {
                if (newOrderForm.getQuantity()>service.getCustomMaxPiece() ||
                        newOrderForm.getQuantity()<service.getCustomMinPiece()){
                    throw new DetectedException("Miktar ilgili servis için belirlenen aralığın dışında !");
                }
            }

            User user = userService.getAuthUser();

            Order order = orderAssembler.convertFormDtoToServiceOrder(newOrderForm, user, service);

            if (user.getBalance() < order.getCustomPrice()){
                log.error("OrderService createServiceOrder Error -> Insufficient Balance !");
                throw new DetectedException("Sipariş için yeterli bakiyeye sahip değilsiniz !");
            }

            if( order.getApiPrice() > service.getApi().getBalance() ){
                log.error("OrderService createServiceOrder Error -> API Insufficient Balance !");
                throw new DetectedException("İşleminiz gerçekleştirilemedi. " +
                        "Bir destek talebi açıp sistem yöneticisine sipariş hakkında bilgi verip yardım isteyebilirsiniz.");
            }

            String apiOrderId = panelService.createOrderReturnOrderId(order, CreateAPIOrderType.SERVICE);

            if (apiOrderId == null || apiOrderId.equals("0")){
                throw new DetectedException("Şu anda seçtiğiniz servis için sipariş alamıyoruz. Daha sonra tekrar deneyin.");
            }

            order.setApiOrderId(apiOrderId);

            order = orderRepository.save(order);

            if (order != null){
                double newBalance = user.getBalance() - order.getCustomPrice();
                userService.adminBasedUpdate(user.getId(), user, String.valueOf(newBalance));

                apiService.asyncApiUpdateBalance(order.getService().getApi());
            }else {
                log.error("OrderService createServiceOrder Order Error -> order not save");
            }

            return true;
        }catch (Exception e){
            if (e instanceof DetectedException)
                throw e;
            log.error("OrderService createServiceOrder Order Error -> " + e.getMessage());
            return false;
        }
    }

    private boolean newOrderFormValidate(NewOrderFormDTO newOrderFormDTO){
        if (isNullOrEmpty(newOrderFormDTO.getUrl()) || newOrderFormDTO.getQuantity() <= 0){
            throw new DetectedException("Tüm alanları doğru girmelisiniz !");
        }else if (newOrderFormDTO.getUrl().length()>400 || newOrderFormDTO.getQuantity()>50000){
            throw new DetectedException("Tüm alanları doğru girmelisiniz !");
        }
        return true;
    }

    private static boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }



    public long createVisitorPackageOrderReturnOrderId(Package pkg, String url){
        try {
            User user = userService.getAdmin();

            Order order = orderAssembler.convertVisitorInfoToOrder(pkg, user, url);

            if( order.getApiPrice() > pkg.getService().getApi().getBalance() ){
                log.error("OrderService createVisitorPackageOrderReturnOrderId Error -> API Insufficient Balance !");
                throw new DetectedException("Ödeme alındı fakat sipariş verilemedi. Para iadesi için sayfa altında yer alan iletişim bilgilerimizden bize ulaşabilirsiniz.");
            }

            String apiOrderId = panelService.createOrderReturnOrderId(order, CreateAPIOrderType.PACKAGE);

            if (apiOrderId == null){
                log.error("OrderService createVisitorPackageOrderReturnOrderId Error -> Api Order Id Null !");
                throw new DetectedException("Ödeme alındı fakat sipariş verilemedi. Para iadesi için sayfa altında yer alan iletişim bilgilerimizden bize ulaşabilirsiniz.");
            }

            order.setApiOrderId(apiOrderId);

            order = orderRepository.save(order);

            if (order != null){
                apiService.asyncApiUpdateBalance(order.getService().getApi());
            }else {
                log.error("OrderService createVisitorPackageOrderReturnOrderId Order Error -> order not save");
            }

            return order.getId();
        }catch (Exception e){
            if (e instanceof DetectedException)
                throw e;
            log.error("OrderService createVisitorPackageOrderReturnOrderId Order Error -> " + e.getMessage());
            return -1;
        }
    }


    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByAuthUser() throws LoginException {
        User user = userService.getAuthUser();

        return orderRepository.findByUser(user, new Sort(Sort.Direction.DESC, "id"));
    }

    public List<UserPageOrderDTO> getUserPageOrderByAuthUser() throws LoginException {
        List<Order> orders = getOrdersByAuthUser();
        return orderAssembler.convertOrdersToUserTypeOrders(orders);
    }

    public List<Order> getOrdersByUser(User user){
        return orderRepository.findByUser(user, new Sort(Sort.Direction.DESC, "id"));
    }

    public Order findById(long id){
        Optional<Order> opt = orderRepository.findById(id);

        if( !opt.isPresent()){
            log.error("Order Service findbyId Error -> Not Found !");
            return null;
        }else
            return opt.get();
    }

    public VisitorPageOrderDTO getVisitorOrderById(String orderIdStr){
        try {
            if (orderIdStr.length()>10)
                throw new RuntimeException();

            long orderId = Long.valueOf(orderIdStr);

            Order order = findById(orderId);

            if (order == null)
                throw new RuntimeException();

            return orderAssembler.convertOrderToVisitorPage(order);
        }catch (Exception e){
            throw new DetectedException("Böyle bir sipariş bulunamadı !");
        }
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
                OrderStatusResponse orderStatusResponse = panelService.orderStatus(order.getApiOrderId(), order.getService().getApi());

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
                else if (orderStatusResponse.getStatus().equals("In progress")){
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

                    order.setRemainBalance(order.getCustomPrice());

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

                order = orderRepository.save(order);
            }
        }catch (Exception e){
            log.error("Order Service Order Status Update Error -> " + e.getMessage());
        }
    }
}
