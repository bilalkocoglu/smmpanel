package com.thelastcodebenders.follower.controller;

import com.thelastcodebenders.follower.dto.*;
import com.thelastcodebenders.follower.dto.tickets.UserTicket;
import com.thelastcodebenders.follower.enums.RoleType;
import com.thelastcodebenders.follower.model.Announcement;
import com.thelastcodebenders.follower.model.Service;
import com.thelastcodebenders.follower.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.security.auth.login.LoginException;

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

    public UserController(ServiceService serviceService,
                          AskedQuestionService askedQuestionService,
                          UserService userService,
                          AnnouncementService announcementService,
                          PaymentNotificationService paymentNotificationService,
                          BankAccountService bankAccountService,
                          TicketService ticketService,
                          OrderService orderService){
        this.serviceService = serviceService;
        this.askedQuestionService = askedQuestionService;
        this.userService = userService;
        this.announcementService = announcementService;
        this.paymentNotificationService = paymentNotificationService;
        this.bankAccountService = bankAccountService;
        this.ticketService = ticketService;
        this.orderService = orderService;
    }

    //USER INDEX
    @GetMapping("/dashboard")       //INDEX PAGE
    public String userIndex(Model model) throws LoginException{
        model.addAttribute("loading_order_count", orderService.getAuthUserActiveOrderCount());
        model.addAttribute("orders", orderService.getOrdersByAuthUser());
        model.addAttribute("username", userService.getAuthUserName());
        model.addAttribute("userbalance", userService.getAuthUserBalance());
        model.addAttribute("unconfirmed_payment_ntf", paymentNotificationService.unconfirmedLoginUserNotifications());
        model.addAttribute("unread_ticket_count", ticketService.unreadTickets(RoleType.USER));
        model.addAttribute("announcements", announcementService.allAskedQuestions());
        model.addAttribute("page", "homepage");
        return "user-index";
    }


    //USER SERVİCES
    @GetMapping("/services")        //SERVİCES PAGE
    public String newOrderPage(Model model) throws LoginException{
        model.addAttribute("neworderform", new NewOrderFormDTO());
        model.addAttribute("username", userService.getAuthUserName());
        model.addAttribute("userbalance", userService.getAuthUserBalance());
        model.addAttribute("service_list_items", serviceService.createUserServicesItems());
        model.addAttribute("page", "services");
        return "user-create-order";
    }

    @GetMapping("/services/service")      //AJAX SERVICE
    public @ResponseBody UserPageServiceDTO serviceById(@RequestParam("serviceId") long id){
        return serviceService.createUserPageServiceFormat(id);
    }


    //USER ORDERS
    @GetMapping("/orders")      //ORDER PAGE
    public String userOrders(Model model) throws LoginException {
        model.addAttribute("username", userService.getAuthUserName());
        model.addAttribute("userbalance", userService.getAuthUserBalance());
        model.addAttribute("page", "orders");
        model.addAttribute("orders", orderService.getOrdersByAuthUser());
        return "user-orders";
    }

    @PostMapping("/order/{serviceId:.*}")       //CREATE ORDER
    public String createNewOrder(@ModelAttribute NewOrderFormDTO orderForm,
                                 @PathVariable("serviceId") long serviceId,
                                 RedirectAttributes redirectAttributes){
        try {
            boolean res = orderService.createOrder(orderForm, serviceId);
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
            else
                redirectAttributes.addFlashAttribute("errormessage", "İşlem gerçekleştirilemedi ! Lürfen daha sonra tekrar deneyiniz !");
        }catch (Exception e){
            if (e instanceof RuntimeException){
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            }else
                redirectAttributes.addFlashAttribute("errormessage", "İşlem gerçekleştirilemedi ! Lürfen daha sonra tekrar deneyiniz !");
        }
        return "redirect:/user/orders";
    }



    //USER ASKED QUESTİON
    @GetMapping("/asked-questions")         //ASKED QUESTİON PAGE
    public String askedQuestionsPage(Model model) throws LoginException{
        model.addAttribute("username", userService.getAuthUserName());
        model.addAttribute("userbalance", userService.getAuthUserBalance());
        model.addAttribute("asked_questions", askedQuestionService.allAskedQuestions());
        model.addAttribute("page", "askedquestions");
        return "user-asked-questions";
    }




    //USER PAYMENT NOTIFICATION
    @GetMapping("/payment-notifications")       //PAYMENT NOTIFICATION PAGE
    public String paymentNotification(Model model) throws LoginException{
        model.addAttribute("username", userService.getAuthUserName());
        model.addAttribute("userbalance", userService.getAuthUserBalance());
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
        model.addAttribute("username", userService.getAuthUserName());
        model.addAttribute("userbalance", userService.getAuthUserBalance());
        model.addAttribute("usertickets", ticketService.allUserTicket());
        model.addAttribute("create_ticket_form", new CreateTicketFormDTO());
        model.addAttribute("page", "tickets");
        return "user-tickets";
    }

    @PostMapping("/tickets")        //CREATE TİCKET
    public String createTicket(@ModelAttribute CreateTicketFormDTO createTicketFormDTO, RedirectAttributes redirectAttributes) throws LoginException {
        boolean res = ticketService.createTicket(createTicketFormDTO);
        if (res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarılı bir şekilde gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata ile karşılaşıldı. Lütfen daha sonra tekrar deneyin.");
        return "redirect:/user/tickets";
    }

    @GetMapping("/tickets/detail/{ticketId:.*}")        //TICKET DETAİL PAGE
    public String ticketDetail(@PathVariable("ticketId") long ticketId, Model model, RedirectAttributes redirectAttributes) throws LoginException {
        UserTicket userTicket = ticketService.oneTicket(ticketId);
        if (userTicket == null){
            redirectAttributes.addFlashAttribute("errormessage", "Böyle bir talep bulunamadı !");
            return "redirect:/user/tickets";
        }else {
            model.addAttribute("username", userService.getAuthUserName());
            model.addAttribute("userbalance", userService.getAuthUserBalance());
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
        model.addAttribute("username", userService.getAuthUserName());
        model.addAttribute("userbalance", userService.getAuthUserBalance());
        model.addAttribute("page", "loadbalance");
        return "user-load-balance";
    }

}
