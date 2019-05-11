package com.thelastcodebenders.follower.controller;

import com.iyzipay.model.CheckoutForm;
import com.iyzipay.model.CheckoutFormInitialize;
import com.iyzipay.model.Status;
import com.thelastcodebenders.follower.dto.*;
import com.thelastcodebenders.follower.dto.tickets.UserTicket;
import com.thelastcodebenders.follower.enums.RoleType;
import com.thelastcodebenders.follower.iyzipay.PaymentService;
import com.thelastcodebenders.follower.model.DrawPrize;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = UserController.END_POINT)
public class UserController {
    public static final String END_POINT = "/user";
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private ServiceService serviceService;
    private AskedQuestionService askedQuestionService;
    private UserService userService;
    private AnnouncementService announcementService;
    private PaymentNotificationService paymentNotificationService;
    private BankAccountService bankAccountService;
    private TicketService ticketService;
    private OrderService orderService;
    private PackageService packageService;
    private DrawService drawService;
    private DrawPrizeService drawPrizeService;
    private ApiService apiService;
    private PaymentService paymentService;
    private CardPaymentService cardPaymentService;

    public UserController(ServiceService serviceService,
                          AskedQuestionService askedQuestionService,
                          UserService userService,
                          AnnouncementService announcementService,
                          PaymentNotificationService paymentNotificationService,
                          BankAccountService bankAccountService,
                          TicketService ticketService,
                          OrderService orderService,
                          PackageService packageService,
                          DrawService drawService,
                          DrawPrizeService drawPrizeService,
                          ApiService apiService,
                          PaymentService paymentService,
                          CardPaymentService cardPaymentService){
        this.serviceService = serviceService;
        this.askedQuestionService = askedQuestionService;
        this.userService = userService;
        this.announcementService = announcementService;
        this.paymentNotificationService = paymentNotificationService;
        this.bankAccountService = bankAccountService;
        this.ticketService = ticketService;
        this.orderService = orderService;
        this.packageService = packageService;
        this.drawService = drawService;
        this.drawPrizeService = drawPrizeService;
        this.apiService = apiService;
        this.paymentService = paymentService;
        this.cardPaymentService = cardPaymentService;
    }

    //USER INDEX
    @GetMapping("/dashboard")       //INDEX PAGE
    public String userIndex(Model model) throws LoginException{
        User user = userService.getAuthUser();

        model.addAttribute("username", user.getName() + ' ' + user.getSurname());
        model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));

        model.addAttribute("loading_order_count", orderService.getAuthUserActiveOrderCount());
        model.addAttribute("orders", orderService.getOrdersByAuthUser());
        model.addAttribute("unconfirmed_payment_ntf", paymentNotificationService.unconfirmedLoginUserNotifications());
        model.addAttribute("unread_ticket_count", ticketService.unreadTickets(RoleType.USER));
        model.addAttribute("announcements", announcementService.allAskedQuestions());
        model.addAttribute("page", "homepage");
        return "user-index";
    }



    //USER SERVİCES
    @GetMapping("/services")        //SERVİCES PAGE
    public String newOrderPage(Model model) throws LoginException{
        User user = userService.getAuthUser();

        model.addAttribute("username", user.getName() + ' ' + user.getSurname());
        model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));

        model.addAttribute("neworderform", new NewOrderFormDTO());
        model.addAttribute("service_list_items", serviceService.createUserServicesItems());

        model.addAttribute("page", "services");
        return "user-create-order";
    }

    @GetMapping("/services/service")      //AJAX SERVICE
    public @ResponseBody UserPageServiceDTO serviceById(@RequestParam("serviceId") long id){
        return serviceService.createUserpageServiceAjaxFormat(id);
    }

    @GetMapping("/services/package")    //AJAX PACKAGE
    public @ResponseBody UserPagePackageDTO packageById(@RequestParam("packageId") long id){
        return packageService.createUserPageServiceFormat(id);
    }



    //USER ORDERS
    @GetMapping("/orders")      //ORDER PAGE
    public String userOrders(Model model) throws LoginException {
        User user = userService.getAuthUser();

        model.addAttribute("username", user.getName() + ' ' + user.getSurname());
        model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));

        model.addAttribute("orders", orderService.getUserPageOrderByAuthUser());

        model.addAttribute("page", "orders");

        return "user-orders";
    }

    @PostMapping("/order/{serviceId:.*}")       //CREATE ORDER
    public String createNewOrder(@ModelAttribute NewOrderFormDTO orderForm,
                                 @PathVariable("serviceId") long serviceId,
                                 RedirectAttributes redirectAttributes){
        try {
            boolean res = orderService.createServiceOrder(orderForm, serviceId);
            if (res){
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
                return "redirect:/user/orders";
            }
            else{
                redirectAttributes.addFlashAttribute("errormessage", "İşlem gerçekleştirilemedi ! Lürfen daha sonra tekrar deneyiniz !");
                return "redirect:/user/services";
            }
        }catch (Exception e){
            if (e instanceof RuntimeException){
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            }else
                redirectAttributes.addFlashAttribute("errormessage", "İşlem gerçekleştirilemedi ! Lürfen daha sonra tekrar deneyiniz !");
            return "redirect:/user/services";
        }
    }

    @PostMapping("/order/package/{packageId:.*}")        //CREATE ORDER PACKAGE
    public String createNewPackageOrder(@ModelAttribute NewOrderFormDTO orderForm,
                                        @PathVariable("packageId") long packageId,
                                        RedirectAttributes redirectAttributes){
        try {
            boolean res = orderService.createPackageOrder(orderForm, packageId);
            if (res){
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
                return "redirect:/user/orders";
            }
            else{
                redirectAttributes.addFlashAttribute("errormessage", "İşlem gerçekleştirilemedi ! Lürfen daha sonra tekrar deneyiniz !");
                return "redirect:/user/services";
            }
        }catch (Exception e){
            if (e instanceof RuntimeException){
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            }else
                redirectAttributes.addFlashAttribute("errormessage", "İşlem gerçekleştirilemedi ! Lürfen daha sonra tekrar deneyiniz !");
            return "redirect:/user/services";
        }
    }



    //USER ASKED QUESTİON
    @GetMapping("/asked-questions")         //ASKED QUESTİON PAGE
    public String askedQuestionsPage(Model model) throws LoginException{
        User user = userService.getAuthUser();

        model.addAttribute("username", user.getName() + ' ' + user.getSurname());
        model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));

        model.addAttribute("asked_questions", askedQuestionService.allAskedQuestions());

        model.addAttribute("page", "askedquestions");

        return "user-asked-questions";
    }




    //USER PAYMENT NOTIFICATION
    @GetMapping("/payment-notifications")       //PAYMENT NOTIFICATION PAGE
    public String paymentNotification(Model model) throws LoginException{
        User user = userService.getAuthUser();

        model.addAttribute("username", user.getName() + ' ' + user.getSurname());
        model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));

        model.addAttribute("create_payment_form", new PaymentNotificationFormDTO());
        model.addAttribute("payment_notifications", paymentNotificationService.getLoginUserPaymentNotifications());
        model.addAttribute("bank_accounts", bankAccountService.allAccounts());

        model.addAttribute("page", "paymentnotification");

        return "user-payment-notifications";
    }

    @PostMapping("/payment-notifications")      //CREATE PAYMENT NOTIFICATION
    public String createPaymentNotification(@ModelAttribute PaymentNotificationFormDTO paymentNtfForm,
                                            RedirectAttributes redirectAttributes) throws LoginException {
        boolean res = paymentNotificationService.createPaymentNotification(paymentNtfForm);
        if (res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarılı bir şekilde gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata ile karşılaşıldı. Lütfen daha sonra tekrar deneyin.");
        return "redirect:/user/payment-notifications";
    }



    //USER TICKETS
    @GetMapping("/tickets")     //TICKET PAGE
    public String tickets(Model model) throws LoginException {
        User user = userService.getAuthUser();

        model.addAttribute("username", user.getName() + ' ' + user.getSurname());
        model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));
        model.addAttribute("usertickets", ticketService.allTicketsByUser(user));
        model.addAttribute("create_ticket_form", new CreateTicketFormDTO());
        model.addAttribute("page", "tickets");
        return "user-tickets";
    }

    @PostMapping("/tickets")        //CREATE TİCKET
    public String createTicket(@ModelAttribute CreateTicketFormDTO createTicketFormDTO,
                               RedirectAttributes redirectAttributes) throws LoginException {
        boolean res = ticketService.createTicket(createTicketFormDTO);
        if (res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarılı bir şekilde gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata ile karşılaşıldı. Lütfen daha sonra tekrar deneyin.");
        return "redirect:/user/tickets";
    }

    @GetMapping("/tickets/detail/{ticketId:.*}")        //TICKET DETAİL PAGE
    public String ticketDetail(@PathVariable("ticketId") long ticketId,
                               Model model,
                               RedirectAttributes redirectAttributes) throws LoginException {
        UserTicket userTicket = ticketService.oneTicket(ticketId);
        if (userTicket == null){
            redirectAttributes.addFlashAttribute("errormessage", "Böyle bir talep bulunamadı !");
            return "redirect:/user/tickets";
        }else {
            User user = userService.getAuthUser();

            model.addAttribute("username", user.getName() + ' ' + user.getSurname());
            model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));
            model.addAttribute("messagedto", new ChatMessageDTO());
            model.addAttribute("userticket", userTicket);
            return "user-ticket-datail";
        }
    }

    @PostMapping("/tickets/response/{ticketId:.*}")         //RESPONSE TİCKET
    public String responseTicket(@PathVariable("ticketId") long ticketId,
                                 RedirectAttributes redirectAttributes,
                                 @ModelAttribute ChatMessageDTO chatMessage){
        boolean res = ticketService.responseTicket(RoleType.USER, chatMessage.getMessage(), ticketId);

        if (!res)
            redirectAttributes.addFlashAttribute("errormessage", "Mesaj gönderilemedi !");

        return "redirect:/user/tickets/detail/" + ticketId;
    }



    //USER LOAD BALANCE
    @GetMapping("/load-balance")
    public String loadBalance(Model model) throws LoginException {
        User user = userService.getAuthUser();

        model.addAttribute("username", user.getName() + ' ' + user.getSurname());
        model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));
        model.addAttribute("page", "loadbalance");
        return "user-load-balance";
    }

    @PostMapping("/load-balance")
    public String loadBalancePost(Model model,
                                  @RequestParam("balance") String balance,
                                  HttpServletRequest httpServletRequest,
                                  RedirectAttributes redirectAttributes) throws LoginException {
        User user = userService.getAuthUser();

        model.addAttribute("username", user.getName() + ' ' + user.getSurname());
        model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));

        CheckoutFormInitialize checkoutFormInitialize = paymentService.createPayment(user, Integer.valueOf(balance), httpServletRequest.getRemoteAddr());
        System.out.println(checkoutFormInitialize.toString());
        if (!checkoutFormInitialize.getStatus().equals(Status.SUCCESS.getValue())){
            log.error("Checkout Form Initialize Error -> " + checkoutFormInitialize.getErrorMessage());
            redirectAttributes.addFlashAttribute("errormessage",
                    "Şu anda ödeme alma işlemini başlatamadık. Lütfen tekrar deneyiniz veya diğer ödeme yöntemlerini kullanınız. Sorun ile ilgili destek talebi açarsanız arkadaşlarımız kısa süre içinde yardımcı olacaktır.");
            return "redirect:/load-balance";
        }

        model.addAttribute("iyzicoscript", checkoutFormInitialize.getCheckoutFormContent());

        return "user-load-balance-iyzico";
    }

    @PostMapping("/iyzico/callback")
    public String iyzicoCallback(@RequestParam("token") String token,
                                 RedirectAttributes redirectAttributes) throws LoginException {
        User user = userService.getAuthUser();

        CheckoutForm checkoutForm = paymentService.infoPayment(token, "");
        System.out.println(checkoutForm.toString());


        if(checkoutForm.getStatus().equals(Status.SUCCESS.getValue())
                && checkoutForm.getPaymentStatus().equals("SUCCESS")){

            cardPaymentService.newSuccessCardPayment(user, checkoutForm.getPaidPrice().doubleValue(), token);
            userService.updateUserBalance(user, checkoutForm.getPaidPrice().doubleValue());
            drawService.addDrawCount(user);

            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarılı !");
        }else {
            if(checkoutForm.getErrorMessage()!=null)
                redirectAttributes.addFlashAttribute("errormessage", checkoutForm.getErrorMessage());
            else
                redirectAttributes.addFlashAttribute("errormessage", "İşlem başarısız !");
        }

        return "redirect:/user/load-balance";
    }


    //USER PROFİLE
    @GetMapping("/profile")
    public String profilePage(Model model) throws LoginException{
        User user = userService.getAuthUser();

        model.addAttribute("user", user);
        model.addAttribute("username", user.getName() + ' ' + user.getSurname());
        model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));
        return "user-profile";
    }

    @PostMapping("/profile/changePass")     //CHANGE PASSWORD
    public String changePassword(RedirectAttributes redirectAttributes,
                                 @ModelAttribute ChangePasswordDTO changePasswordDTO){
        try {
            userService.changePassword(changePasswordDTO);

            redirectAttributes.addFlashAttribute("successmessage", "İşleminiz başarıyla gerçekleşti !");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
        }
        return "redirect:/user/profile";
    }


    //SPINNER
    @GetMapping("/draw")
    public String draw(Model model) throws LoginException{
        User user = userService.getAuthUser();

        model.addAttribute("username", user.getName() + ' ' + user.getSurname());
        model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));

        model.addAttribute("draworders", drawService.findDrawOrderByUser(user));

        int drawCount = drawService.findDrawCountByUser(user).getCount();
        if (drawCount > 0){
            //bir sonraki için süre veya çekiliş
            CountDownDTO countDownDTO = drawService.drawPermission(user);
            if (countDownDTO != null)
                model.addAttribute("countDown", countDownDTO);
        }
        model.addAttribute("drawCount", drawCount);

        model.addAttribute("page","draw");
        return "user-draw";
    }

    @GetMapping("/draw/action")
    public String drawAction(Model model,
                             RedirectAttributes redirectAttributes) throws LoginException {
        User user = userService.getAuthUser();

        try {
            //bu sayfaya erişmek için izin var mı yok mu
            if (drawService.drawActionPermission(user)){
                model.addAttribute("username", user.getName() + ' ' + user.getSurname());
                model.addAttribute("userbalance", Double.parseDouble(String.format("%.2f", user.getBalance())));

                model.addAttribute("spinnerItems", drawPrizeService.getSpinnerItems());
                model.addAttribute("drawVisitId", drawService.createDrawVisit(user));

                return "user-draw-action";
            }else {
                redirectAttributes.addFlashAttribute("errormessage", "Şu anda çekilişe katılamıyorsunuz. Lütfen daha sonra tekrar deneyin.");
                return "redirect:/user/draw";
            }
        }catch (Exception e){
            if (e instanceof RuntimeException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else
                redirectAttributes.addFlashAttribute("errormessage", "Şu anda çekilişe katılamıyorsunuz. Lütfen daha sonra tekrar deneyin.");
            return "redirect:/user/draw";
        }
    }

    @PostMapping("/draw/action/result/{drawVisitId:.*}/{prizeId:.*}")
    public String drawActionResult(RedirectAttributes redirectAttributes,
                                   @RequestParam("url") String url,
                                   @PathVariable("prizeId") long prizeId,
                                   @PathVariable("drawVisitId") long drawVisitId){
        try {
            DrawPrize drawPrize = drawService.newPrize(url, prizeId, drawVisitId);
            apiService.asyncApiUpdateBalance(drawPrize.getService().getApi());
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
        }
        return "redirect:/user/draw";
    }


}




