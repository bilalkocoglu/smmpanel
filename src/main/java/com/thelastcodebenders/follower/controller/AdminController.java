package com.thelastcodebenders.follower.controller;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.thelastcodebenders.follower.dto.ChatMessageDTO;
import com.thelastcodebenders.follower.dto.PackageFormDTO;
import com.thelastcodebenders.follower.dto.ServiceFormDTO;
import com.thelastcodebenders.follower.dto.tickets.UserTicket;
import com.thelastcodebenders.follower.enums.RoleType;
import com.thelastcodebenders.follower.enums.UserAction;
import com.thelastcodebenders.follower.model.*;
import com.thelastcodebenders.follower.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping(value = AdminController.END_POINT)
public class AdminController {
    public static final String END_POINT = "/admin";
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private TicketService ticketService;
    private PaymentNotificationService paymentNotificationService;
    private UserService userService;
    private ServiceService serviceService;
    private ApiService apiService;
    private BankAccountService bankAccountService;
    private CardPaymentService cardPaymentService;
    private AskedQuestionService askedQuestionService;
    private AnnouncementService announcementService;
    private CategoryService categoryService;
    private PackageService packageService;
    private OrderService orderService;
    private CategoryArticleService categoryArticleService;
    private DrawPrizeService drawPrizeService;

    public AdminController(TicketService ticketService,
                           PaymentNotificationService paymentNotificationService,
                           UserService userService,
                           ServiceService serviceService,
                           ApiService apiService,
                           BankAccountService bankAccountService,
                           CardPaymentService cardPaymentService,
                           AskedQuestionService askedQuestionService,
                           AnnouncementService announcementService,
                           CategoryService categoryService,
                           PackageService packageService,
                           OrderService orderService,
                           CategoryArticleService categoryArticleService,
                           DrawPrizeService drawPrizeService){
        this.ticketService = ticketService;
        this.paymentNotificationService = paymentNotificationService;
        this.userService = userService;
        this.serviceService = serviceService;
        this.apiService = apiService;
        this.bankAccountService = bankAccountService;
        this.cardPaymentService = cardPaymentService;
        this.askedQuestionService = askedQuestionService;
        this.announcementService = announcementService;
        this.categoryService = categoryService;
        this.packageService = packageService;
        this.orderService = orderService;
        this.categoryArticleService = categoryArticleService;
        this.drawPrizeService = drawPrizeService;
    }

    //  ADMIN INDEX
    @GetMapping("/dashboard")       //ADMIN HOME PAGE
    public String home(Model model) throws LoginException {
        model.addAttribute("winnings", orderService.getWinnings());
        model.addAttribute("unread_ticket_count", ticketService.unreadTickets(RoleType.ADMIN));
        model.addAttribute("payment_count", paymentNotificationService.unconfirmedNotifications());
        model.addAttribute("user_count", userService.userCount());
        model.addAttribute("active_service_count", serviceService.activeServiceCount());
        model.addAttribute("api_table_colums", apiService.adminTableColumns());
        model.addAttribute("apis", apiService.getAllAPIs());
        model.addAttribute("page", "homepage");
        return "admin-index";
    }



    //  ADMIN ORDERS
    @GetMapping("/orders")          //ORDER PAGE
    public String orders(Model model){
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("page", "orders");
        return "admin-orders";
    }




    //  ADMIN BANK ACCOUNTS
    @GetMapping("/bankaccounts")        //ACCOUNT PAGE
    public String bankAccounts(Model model){
        model.addAttribute("page", "bank-accounts");
        model.addAttribute("account", new BankAccount());
        model.addAttribute("accounts", bankAccountService.allAccounts());
        return "admin-bank-accounts";
    }

    @PostMapping("/bankaccounts")       //CREATE BANK ACCOUNT
    public String saveAccount(@Valid @ModelAttribute BankAccount bankAccount, RedirectAttributes attributes){
        try {
            bankAccountService.save(bankAccount);
            attributes.addFlashAttribute("successmessage", "Hesap başarılı bir şekilde sisteme kaydedildi !");
        }catch (Exception e){
            attributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        }
        return "redirect:/admin/bankaccounts";
    }

    @GetMapping("/bankaccounts/{accountId}")    //DELETE BANK ACCOUNT
    public String deleteAccount(@PathVariable("accountId") long id, RedirectAttributes redirectAttributes){
        try {
            boolean res = bankAccountService.deleteAccount(id);
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
            else
                redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        }catch (Exception e){
            if (e instanceof RuntimeException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else
                redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        }finally {
            return "redirect:/admin/bankaccounts";
        }
    }




    //  ADMIN PAYMENT NOTIFICATION
    @GetMapping("/paymentnotifications")        //PAYMENT NOTİFİCATİON PAGE
    public String paymentNotifications(Model model){
        model.addAttribute("payment_table_columns", paymentNotificationService.tableColumns());
        model.addAttribute("payments", paymentNotificationService.allPaymentNotifications());
        model.addAttribute("page", "paymentnotifications");
        return "admin-payment-notifications";
    }

    @GetMapping("/paymentnotifications/confirm/{paymentId:.*}")     //PAYMENT CONFİRM
    public String paymentNotificationConfirm(@PathVariable("paymentId") long id, RedirectAttributes redirectAttributes){
        boolean res = paymentNotificationService.confirmPayment(id);
        if (res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        return "redirect:/admin/paymentnotifications";
    }




    //  ADMIN CARD PAYMENTS
    @GetMapping("/cardpayments")        //CARD PAYMENT PAGE
    public String cardPayments(Model model){
        model.addAttribute("payment_table_columns", cardPaymentService.tableColumns());
        model.addAttribute("payments", cardPaymentService.allCardPayment());
        model.addAttribute("page", "cardpayments");
        return "admin-card-payments";
    }




    //  ADMIN USERS
    @GetMapping("/users")           //USERS PAGE
    public String users(Model model){
        model.addAttribute("user_table_columns", userService.tableColumns());
        model.addAttribute("users", userService.allUser());
        model.addAttribute("page", "users");
        return "admin-users";
    }

    @GetMapping("/users/passivate/{userId:.*}")     //USER PASSİVATE
    public String bannedUser(@PathVariable("userId") long id, RedirectAttributes redirectAttributes){
        boolean res = userService.changeState(id, UserAction.PASSIVATE);

        if(res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");

        return "redirect:/admin/users";
    }

    @GetMapping("/users/activate/{userId:.*}")      //USER ACTIVATE
    public String activateUser(@PathVariable("userId") long id, RedirectAttributes redirectAttributes){
        boolean res = userService.changeState(id, UserAction.ACTIVATE);

        if(res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");

        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{userId:.*}")          //EDİT USER GET
    public String editUser(@PathVariable("userId") long id, Model model, RedirectAttributes redirectAttributes){
        User res = userService.findUserById(id);
        if (res == null){
            redirectAttributes.addFlashAttribute("errormessage", "Düzenlemek istediğiniz kullanıcı bulunamadı.");
            return "redirect:/admin/users";
        }else {
            model.addAttribute("user", res);
            model.addAttribute("userid", res.getId());
            model.addAttribute("orders", orderService.getOrdersByUser(res));
            return "admin-edit-user";
        }
    }

    @PostMapping("/users/edit/{userId:.*}")         //UPDATE USER
    public String updateUser(@ModelAttribute User user, @PathVariable("userId") long id , HttpServletRequest request, RedirectAttributes redirectAttributes){
        boolean res = userService.adminBasedUpdate(id, user, request.getParameter("newbalance"));
        if (res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");

        return "redirect:/admin/users/edit/" + id;
    }




    //  ADMIN ASKED QUESTİON
    @GetMapping("/asked-questions")         //ASKED QUESTİON PAGE
    public String askedQuestion(Model model){
        model.addAttribute("question_table_columns", askedQuestionService.tableColumns());
        model.addAttribute("questions", askedQuestionService.allAskedQuestions());
        model.addAttribute("askedquestion", new AskedQuestion());
        model.addAttribute("page", "askedquestion");
        return "admin-asked-questions";
    }

    @PostMapping("/asked-questions")    //create ASKED QUESTİON
    public String createAskedQuestion(@Valid @ModelAttribute AskedQuestion askedQuestion, RedirectAttributes redirectAttributes){
        boolean res = askedQuestionService.save(askedQuestion);
        if (res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        return "redirect:/admin/asked-questions";
    }

    @GetMapping("/asked-questions/remove/{questionId:.*}")      //delete ASKED QUESTİON
    public String deleteAskedQuestion(@PathVariable("questionId") long id, RedirectAttributes redirectAttributes){
        boolean res = askedQuestionService.delete(id);
        if (res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        return "redirect:/admin/asked-questions";
    }




    //  ADMIN ANNOUNCEMENTS
    @GetMapping("/announcements")       //ANNOUNCEMENT PAGE
    public String accouncements(Model model){
        model.addAttribute("announcement_table_columns", announcementService.tableColumns());
        model.addAttribute("announcements", announcementService.allAskedQuestions());
        model.addAttribute("announcement", new Announcement());
        model.addAttribute("page", "announcement");
        return "admin-annuncements";
    }

    @PostMapping("/announcements")      //CREATE ANNOUNCEMENT
    public String createAnnouncement(@Valid @ModelAttribute Announcement announcement, RedirectAttributes redirectAttributes){
        boolean res = announcementService.save(announcement);
        if (res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");

        return "redirect:/admin/announcements";
    }

    @GetMapping("/announcements/remove/{announcementId:.*}")      //DELETE ANNOUNCEMENT
    public String deleteAnnouncement(@PathVariable("announcementId") long id, RedirectAttributes redirectAttributes){
        boolean res = announcementService.delete(id);
        if (res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        return "redirect:/admin/announcements";
    }




    //  ADMIN CATEGORY
    @GetMapping("/categories")      //CATEGORY PAGE
    public String categories(Model model){
        model.addAttribute("subcategory_table_columns", categoryService.subCategoryColums());
        model.addAttribute("maincategory_table_columns", categoryService.categoryColumns());
        model.addAttribute("subcategories", categoryService.allSubcategory());
        model.addAttribute("maincategories", categoryService.allCategory());
        model.addAttribute("subcategory", new SubCategory());
        model.addAttribute("maincategory", new Category());
        model.addAttribute("page", "category");
        return "admin-categories";
    }

    @PostMapping("/categories/subcategory")     //CREATE SUBCATEGORY
    public String createSubcategory(@ModelAttribute SubCategory subCategory, RedirectAttributes redirectAttributes, HttpServletRequest request){
        try {
            boolean res = categoryService.saveSubcategory(subCategory, request.getParameter("categoryId"));
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
            else
                redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        }catch (Exception e){
            if (e instanceof RuntimeException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else
                redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        }finally {
            return "redirect:/admin/categories";
        }
    }

    @GetMapping("/categories/subcategory")      //AJAX SUBCATEGORİES
    public @ResponseBody List<SubCategory> subCategoriesByMainCategory(@RequestParam("maincategoryId") long id){
        return categoryService.findSubCategoryByMainCategory(id);
    }

    @PostMapping("/categories/maincategory")    //CREATE CATEGORY
    public String createMainCategory(@Valid @ModelAttribute Category category, RedirectAttributes redirectAttributes){
        try {
            boolean res = categoryService.saveCategory(category);
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
            else
                redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        }catch (Exception e){
            if(e instanceof RuntimeException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else
                redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        }finally {
            return "redirect:/admin/categories";
        }
    }

    @GetMapping("/categories/subcategory/delete/{subcategoryId:.*}")       //DELETE SUBCATEGORY
    public String deleteSubcategory(@PathVariable("subcategoryId") long subcategoryId, RedirectAttributes redirectAttributes){
        try {
            boolean res = categoryService.deleteSubcategory(subcategoryId);
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
            else
                redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
        }finally {
            return "redirect:/admin/categories";
        }
    }

    @GetMapping("/categories/maincategory/delete/{mainCategoryId:.*}")       //DELETE MAİNCATEGORY
    public String deleteMainCategory(@PathVariable("mainCategoryId") long mainCategoryId, RedirectAttributes redirectAttributes){
        try {
            boolean res = categoryService.deleteMainCategory(mainCategoryId);
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
            else
                redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
        }finally {
            return "redirect:/admin/categories";
        }
    }




    //  ADMIN APIS
    @GetMapping("/apis")        //APİ PAGE
    public String apis(Model model){
        model.addAttribute("api_table_colums", apiService.apiTableColumns());
        model.addAttribute("apis", apiService.getAllAPIs());
        model.addAttribute("newapi", new API());
        model.addAttribute("page", "api");
        return "admin-apis";
    }

    @PostMapping("/apis")       //CREATE API
    public String createApi(@ModelAttribute API api, RedirectAttributes redirectAttributes){
        try {
            boolean res = apiService.save(api);
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
            else
                redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
            return "redirect:/admin/apis";
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
        }finally {
            return "redirect:/admin/apis";
        }
    }

    @GetMapping("/apis/delete/{apiId:.*}")      //DELETE API
    public String deleteApi(@PathVariable("apiId") long apiId, RedirectAttributes redirectAttributes){
        try {
            boolean res = apiService.deleteApi(apiId);
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
            else
                redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
        }finally {
            return "redirect:/admin/apis";
        }
    }

    @GetMapping("/apis/passivate/{apiId:.*}")       //PASSIVATE API
    public String passivateApi(@PathVariable("apiId") long apiId, RedirectAttributes redirectAttributes){
        boolean res = apiService.changeState(apiId, UserAction.PASSIVATE);

        if(res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");

        return "redirect:/admin/apis";
    }

    @GetMapping("/apis/activate/{apiId:.*}")        //ACTIVATE API
    public String activateApi(@PathVariable("apiId") long apiId, RedirectAttributes redirectAttributes){
        boolean res = apiService.changeState(apiId, UserAction.ACTIVATE);

        if(res)
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        else
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Daha sonra tekrar deneyin !");

        return "redirect:/admin/apis";
    }

    @GetMapping("/apis/update-balance")     //ALL API UPDATE BALANCE
    public String updateBalanceApi(RedirectAttributes redirectAttributes){
        String message = apiService.allApiUpdateBalance();
        redirectAttributes.addFlashAttribute("infomessage", message);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/apis/update-services")    //ALL API UPDATE SERVİCE
    public String updateServiceApi(RedirectAttributes redirectAttributes){
        try {
            String message = apiService.allApiUpdateService();
            if (message.equals("")){
                redirectAttributes.addFlashAttribute("successmessage", "Servisler başarılı şekilde güncellendi, servislerde bir değişiklik görülmedi.");
                return "redirect:/admin/dashboard";
            }else {
                redirectAttributes.addFlashAttribute("infomessage", message);
                return "redirect:/admin/services";
            }
        }catch (Exception e){
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errormessage", "Beklenmeyen bir hata oluştu. Servisler güncellenemedi !");
            return "redirect:/admin/dashboard";
        }
    }




    //ADMIN SERVİCES
    @GetMapping("/services")        //SERVİCES PAGE
    public String services(Model model){
        model.addAttribute("service_table_columns", serviceService.serviceColumns());
        model.addAttribute("services", serviceService.allService());
        model.addAttribute("page", "service");
        return "admin-services";
    }

    @GetMapping("/services/edit/{serviceId:.*}")        //SERVİCES EDİT
    public String editService(@PathVariable("serviceId") long serviceId, Model model, RedirectAttributes redirectAttributes){
        try {
            Service service = serviceService.findServiceById(serviceId);
            if (service == null){
                redirectAttributes.addFlashAttribute("errormessage", "Düzenlemek istediğiniz servis bulunamadı !");
                return "redirect:/admin/services";
            }else{
                model.addAttribute("service", service);
                model.addAttribute("serviceform", serviceService.createFormDto(service));
                model.addAttribute("categories", categoryService.allSubcategory());
                model.addAttribute("page", "serviceedit");
                return "admin-services-edit";
            }
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errormessage", "Düzenlemek istediğiniz servis bulunamadı !");
            return "redirect:/admin/services";
        }
    }

    @GetMapping("/services/service-category")        //AJAX SERVİCE BY CATEGORY
    public @ResponseBody List<Service> findServiceByCategory(@RequestParam("subcategoryId") long id){
        return serviceService.findActiveServiceByCategory(id);
    }

    @GetMapping("/services/service-id")             //AJAX SERVİCE BY ID
    public @ResponseBody Service findServiceById(@RequestParam("serviceId") long id){
        return serviceService.findServiceById(id);
    }

    @PostMapping("/services/update/{serviceId:.*}")     //UPDATE SERVİCE
    public String updateService(@PathVariable("serviceId") long serviceId,
                                @Valid @ModelAttribute ServiceFormDTO serviceFormDTO,
                                RedirectAttributes redirectAttributes){
        try {
            boolean res = serviceService.updateService(serviceFormDTO, serviceId);
            if(res)
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
            else
                redirectAttributes.addFlashAttribute("errormessage", "İşlem şuan gerçekleştirilemedi, daha sonra tekrar deneyiniz.");
        }catch (Exception e){
            if (e instanceof RuntimeException)
                redirectAttributes.addFlashAttribute("errormessage",e.getMessage());
            else
                redirectAttributes.addFlashAttribute("errormessage", "İşlem şuan gerçekleştirilemedi, daha sonra tekrar deneyiniz.");
        }finally {
            return "redirect:/admin/services";
        }
    }



    //ADMIN PACKAGES
    @GetMapping("/packages")        //PACKAGES PAGE
    public String packages(Model model){
        model.addAttribute("page", "packages");
        model.addAttribute("maincategories", categoryService.allCategory());
        model.addAttribute("packageform", new PackageFormDTO());
        model.addAttribute("packages", packageService.allPackages());
        model.addAttribute("package_columns", packageService.packageColumns());
        return "admin-packages";
    }

    @PostMapping("/packages")       //CREATE PACKAGE
    public String createPackage(@ModelAttribute PackageFormDTO packageFormDTO, RedirectAttributes redirectAttributes){
        boolean res = packageService.save(packageFormDTO);
        if (res){
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        }else {
            redirectAttributes.addFlashAttribute("errormessage", "İşlem şuan gerçekleştirilemedi, daha sonra tekrar deneyiniz.");
        }
        return "redirect:/admin/packages";
    }

    @GetMapping("/packages/passivate/{packageId:.*}")       //PASSİVATE PACKAGE
    public String passivatePackage(@PathVariable("packageId") long pkgId, RedirectAttributes redirectAttributes){
        boolean res = packageService.changeState(pkgId, UserAction.PASSIVATE);
        if (res){
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        }else {
            redirectAttributes.addFlashAttribute("errormessage", "İşlem şuan gerçekleştirilemedi, daha sonra tekrar deneyiniz.");
        }
        return "redirect:/admin/packages";
    }

    @GetMapping("/packages/activate/{packageId:.*}")        //ACTIVATE PACKAGE
    public String activatePackage(@PathVariable("packageId") long pkgId, RedirectAttributes redirectAttributes){
        try {
            boolean res = packageService.changeState(pkgId, UserAction.ACTIVATE);
            if (res){
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
            }else {
                redirectAttributes.addFlashAttribute("errormessage", "İşlem şuan gerçekleştirilemedi, daha sonra tekrar deneyiniz.");
            }
        }catch (Exception e){
            if (e instanceof RuntimeException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else  {
                redirectAttributes.addFlashAttribute("errormessage", "İşlem şuan gerçekleştirilemedi, daha sonra tekrar deneyiniz.");
            }
        }finally {
            return "redirect:/admin/packages";

        }
    }




    // ADMIN TICKETS
    @GetMapping("/tickets")         //TICKETS PAGE
    public String tickets(Model model){
        model.addAttribute("usertickets", ticketService.allAdminTicket());
        model.addAttribute("page", "tickets");
        return "admin-tickets";
    }

    @GetMapping("/tickets/detail/{ticketId:.*}")        //TICKET DETAİL PAGE
    public String ticketDetail(@PathVariable("ticketId") long ticketId, Model model, RedirectAttributes redirectAttributes){
        UserTicket userTicket = ticketService.oneTicket(ticketId);
        if (userTicket == null){
            redirectAttributes.addFlashAttribute("errormessage", "Böyle bir talep bulunamadı !");
            return "redirect:/admin/tickets";
        }else {
            model.addAttribute("messagedto", new ChatMessageDTO());
            model.addAttribute("userticket", userTicket);
            return "admin-ticket-datail";
        }
    }

    @PostMapping("/tickets/response/{ticketId:.*}")         //RESPONSE TİCKET
    public String responseTicket(@PathVariable("ticketId") long ticketId,
                                 RedirectAttributes redirectAttributes,
                                 @ModelAttribute ChatMessageDTO chatMessage){
        boolean res = ticketService.responseTicket(RoleType.ADMIN, chatMessage.getMessage(), ticketId);

        if (!res)
            redirectAttributes.addFlashAttribute("errormessage", "Mesaj gönderilemedi !");

        return "redirect:/admin/tickets/detail/" + ticketId;
    }

    @GetMapping("/tickets/close/{ticketId:.*}")         //CLOSE TİCKET
    public String closeTicket(@PathVariable("ticketId") long ticketId, RedirectAttributes redirectAttributes){
        boolean res = ticketService.closeTicket(ticketId);
        if (!res)
            redirectAttributes.addFlashAttribute("errormessage", "Konuşma başarılı bir şekilde sonlandırılamadı !");
        else
            redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
        return "redirect:/admin/tickets";
    }





    //ADMIN CATEGORY ARTICLE
    @GetMapping("/articles")            //CATEGORY ARTICLE PAGE
    public String articles(Model model){
        model.addAttribute("emptyArticleForm", new CategoryArticle());
        model.addAttribute("emptyCategories", categoryArticleService.emptyCategories());
        model.addAttribute("articles", categoryArticleService.findAll());
        model.addAttribute("page", "articles");
        return "admin-articles";
    }

    @PostMapping("/article/create")     //CREATE CATEGORY ARTICLE
    public String createArticles(RedirectAttributes redirectAttributes,
                                 @ModelAttribute CategoryArticle categoryArticle){
        try {
            boolean res = categoryArticleService.save(categoryArticle);
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "Makale Yayınlandı !");
            else
                redirectAttributes.addFlashAttribute("errormessage", "İşleminiz şu anda gerçekleştirilemedi. Lütfen daha sonra tekrar deneyin.");
        }catch (Exception e){
            log.error("/admin/article/create Error -> " + e.getMessage());
            redirectAttributes.addFlashAttribute("errormessage", "İşleminiz şu anda gerçekleştirilemedi. Lütfen daha sonra tekrar deneyin.");
        }

        return "redirect:/admin/articles";
    }

    @GetMapping("/article/edit/{categoryId:.*}")    //ARTİCLE EDİT PAGE
    public String editArticlePage(Model model,
                                  @PathVariable("categoryId") long categoryId,
                                  RedirectAttributes redirectAttributes){
        try {
            model.addAttribute("selectedarticle", categoryArticleService.findByCategoryId(categoryId));
            model.addAttribute("articles", categoryArticleService.findAll());
            model.addAttribute("page", "articles");
            return "admin-articles";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errrormessage", e.getMessage());
            return "redirect:/admin/articles";
        }
    }

    @PostMapping("/article/edit/{categoryArticleId:.*}")        //UPDATE ARTİCLE
    public String updateArticle(@PathVariable("categoryArticleId") long categoryArticleId,
                                @RequestParam("article") String articleBody,
                                RedirectAttributes redirectAttributes){
        try {
            boolean res = categoryArticleService.update(categoryArticleId, articleBody);

            if( res )
                redirectAttributes.addFlashAttribute("successmessage", "Yazı başarıyla güncellendi.");
            else
                redirectAttributes.addFlashAttribute("errormessage", "İşleminiz gerçekleştirilemedi. Lütfen daha sonra tekrar deneyin !");
        }catch (Exception e){
            if (e instanceof RuntimeException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else {
                log.error("Admin Controller UpdateArticle Error -> " + e.getMessage());
                redirectAttributes.addFlashAttribute("errormessage", "İşleminiz gerçekleştirilemedi. Lütfen daha sonra tekrar deneyin !");
            }
        }
        return "redirect:/admin/articles";
    }



    //DRAW SETTİNGS
    @GetMapping("/draw-settings")
    public String drawSettings(Model model){
        model.addAttribute("drawPrizes", drawPrizeService.findAll());
        model.addAttribute("maincategories", categoryService.allCategory());
        model.addAttribute("page", "drawsetting");
        return "admin-draw-settings";
    }

    @PostMapping("/draw-prize")
    public String createDrawPrize(RedirectAttributes redirectAttributes,
                                  @ModelAttribute DrawPrize drawPrize){
        try {
            drawPrizeService.save(drawPrize);
            redirectAttributes.addFlashAttribute("infomessage", "Ödül başarıyla PASİF olarak eklendi !");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
        }
        return "redirect:/admin/draw-settings";
    }
}

