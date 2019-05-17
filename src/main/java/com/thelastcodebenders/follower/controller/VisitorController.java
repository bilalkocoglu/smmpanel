package com.thelastcodebenders.follower.controller;

import com.iyzipay.model.CheckoutForm;
import com.iyzipay.model.CheckoutFormInitialize;
import com.iyzipay.model.Status;
import com.thelastcodebenders.follower.dto.*;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.payment.iyzipay.PaymentService;
import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.model.VisitorUser;
import com.thelastcodebenders.follower.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
public class VisitorController {
    private static final Logger log = LoggerFactory.getLogger(VisitorController.class);

    private UserService userService;
    private PackageService packageService;
    private CategoryService categoryService;
    private VisitorMessageService visitorMessageService;
    private CategoryArticleService categoryArticleService;
    private PaymentService paymentService;
    private VisitorUserService visitorUserService;
    private OrderService orderService;
    private MailService mailService;

    public VisitorController(UserService userService,
                             PackageService packageService,
                             CategoryService categoryService,
                             VisitorMessageService visitorMessageService,
                             CategoryArticleService categoryArticleService,
                             PaymentService paymentService,
                             VisitorUserService visitorUserService,
                             OrderService orderService,
                             MailService mailService){
        this.userService = userService;
        this.packageService = packageService;
        this.categoryService = categoryService;
        this.visitorMessageService = visitorMessageService;
        this.categoryArticleService = categoryArticleService;
        this.paymentService = paymentService;
        this.visitorUserService = visitorUserService;
        this.orderService = orderService;
        this.mailService = mailService;
    }

    //Login
    @GetMapping("/login")
    public String loginPage(Model model){
        return "visitor-login";
    }

    //AUTH Failure
    @GetMapping("/login-failure/{errorcode:.*}")
    public String loginFailure(RedirectAttributes redirectAttributes,
                               @PathVariable("errorcode") int errorcode){
        if (errorcode == 1){
            redirectAttributes.addFlashAttribute("errormessage", "Hesabınız aktif değil !");
        }else if (errorcode == 2){
            redirectAttributes.addFlashAttribute("errormessage", "Kullanıcı adı veya şifre hatalı !");
        }
        return "redirect:/login";
    }

    //Forgot Password Page
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model){
        return "visitor-forgot-password";
    }

    //Forgot Password Action
    @PostMapping("/forgot-password")
    public String resetPassword(RedirectAttributes redirectAttributes,
                                @RequestParam("email") String email){
        try {
            boolean res = userService.resetPassword(email);
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "Yeni şifrenizi size mail olarak gönderdik.");
            else
                redirectAttributes.addFlashAttribute("errormessage", "İşleminizi şu anda gerçekleştiremedik. Lütfen daha sonra tekrar deneyiniz !");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
        }
        return "redirect:/forgot-password";
    }


    @GetMapping("/account-activate/again")
    public String accountActivateAgainPage(Model model){
        return "visitor-activate-again";
    }

    @PostMapping("/account-activate/again")
    public String accountActivateAgain(RedirectAttributes redirectAttributes,
                                       @RequestParam("email") String email){
        try {
            userService.accountActivateMailAgain(email);
            redirectAttributes.addFlashAttribute("successmessage", "Aktifleştirme linkiniz mail olarak gönderildi !");
        }catch (Exception e){
            if (e instanceof DetectedException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else {
                log.error(e.getMessage());
                redirectAttributes.addFlashAttribute("errormessage", "İşleminiz şu anda gerçekleştirilemedi. Lütfen daha sonra tekrar deneyin.");
            }
        }
        return "redirect:/account-activate/again";
    }


    //Register Page
    @GetMapping("/registration")
    public String registrationPage(Model model){
        model.addAttribute("userform", new RegisterFormDTO());
        return "visitor-register";
    }

    //Create User
    @PostMapping("/registration")
    public String createUser(@ModelAttribute RegisterFormDTO registerFormDTO,
                             RedirectAttributes redirectAttributes){
        try {
            boolean res = userService.saveUser(registerFormDTO);
            if (res){
                redirectAttributes.addFlashAttribute("successmessage", "Kaydınız tamamlandı ! Hesabınızı onaylamanız için size bir mail gönderdik.");
                return "redirect:/login";
            }
            else{
                redirectAttributes.addFlashAttribute("errormessage", "İşlem şuan gerçekleştirilemedi, daha sonra tekrar deneyiniz.");
                return "redirect:/registration";
            }
        }catch (Exception e){
            if (e instanceof DetectedException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else
                redirectAttributes.addFlashAttribute("errormessage", "İşlem şuan gerçekleştirilemedi, daha sonra tekrar deneyiniz.");
            return "redirect:/registration";
        }
    }

    //Account Activate
    @GetMapping("/account-activate/{code}")
    public String accountActivateWithMail(@PathVariable("code") String secretKey, RedirectAttributes redirectAttributes){
        try {
            boolean res = userService.accountActivateWithMail(secretKey);
            if (res){
                redirectAttributes.addFlashAttribute("successmessage", "Hesabınız doğrulandı !");
                return "redirect:/login";
            } else
                return "redirect:/";
        }catch (Exception e){
            if (e instanceof DetectedException){
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
                return "redirect:/login";
            }else
                return "redirect:/";
        }
    }


    //Index
    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("packages", packageService.activePackagesTop12());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        model.addAttribute("message", new VisitorMessageDTO());
        return "visitor-index";
    }


    //All Packages
    @GetMapping("/all-packages")
    public String allPackages(Model model){
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        model.addAttribute("categories", packageService.visitorAllPackageCategories());
        return "visitor-allpackages";
    }


    //Package
    @GetMapping("/package/{categoryUrlName}")       //Category packages page
    public String packagesByCategory(@PathVariable("categoryUrlName") String categoryUrlName,
                                     Model model,
                                     RedirectAttributes redirectAttributes){
        try {
            Category category = categoryService.visitorPackagePageCategory(categoryUrlName);
            if (packageService.isValidatePackageCategory(category)){
                model.addAttribute("packageItems", packageService.createVisitorPackagesFormat(category));
                try {
                    model.addAttribute("categoryArticle", categoryArticleService.findByCategoryId(category.getId()));
                }catch (Exception e){
                    log.error("Visitor Controller PackageByCategory Error -> " + e.getMessage());
                }
            }

            model.addAttribute("category", category);
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            return "redirect:/all-packages";
        }
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        return "visitor-packages";
    }

    @GetMapping("/package/order/{packageId:.*}")        //Package Order Payment Page
    public String packageOrderPage(@PathVariable("packageId") long packageId,
                                   Model model,
                                   RedirectAttributes redirectAttributes){
        Package pkg = packageService.findById(packageId);
        if (pkg == null || !pkg.isState()){
            redirectAttributes.addFlashAttribute("errormessage", "Bu siparişi şu anda alamıyoruz lütfen daha sonra tekrar deneyin.");
            return "redirect:/all-packages";
        }

        double apiPrice = (pkg.getService().getApiPrice()/1000) * pkg.getQuantity();
        //System.out.println(apiPrice);
        //System.out.println(pkg.getService().getApi().getBalance());
        if ( pkg.getService().getApi().getBalance() < apiPrice){
            log.error("Api bakiyesi yetersiz !");
            redirectAttributes.addFlashAttribute("errormessage", "İstediğiniz paket için geçici bir süre sipariş alamıyoruz. Lütfen daha sonra tekrar deneyin.");
            return "redirect:/all-packages";
        }

        model.addAttribute("pkg", packageService.createUserPageServiceFormat(packageId));
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        model.addAttribute("message", new VisitorMessageDTO());
        return "visitor-package-order";
    }

    @PostMapping("/package/order/{packageId:.*}/payment")
    public String packageOrderPaymentPage(RedirectAttributes redirectAttributes,
                                          @PathVariable("packageId") long packageId,
                                          @ModelAttribute PackageOrderPaymentFormDTO packageOrderPaymentForm,
                                          HttpServletRequest request,
                                          Model model){
        try {
            //find package
            Package pkg = packageService.findById(packageId);
            if (pkg == null || !pkg.isState()){
                redirectAttributes.addFlashAttribute("errormessage", "Bu siparişi şu anda alamıyoruz lütfen daha sonra tekrar deneyin.");
                return "redirect:/all-packages";
            }

            //validation
            //create visit user
            VisitorUser visitorUser = visitorUserService.save(packageOrderPaymentForm, packageId);

            //create iyzipay payment page
            CheckoutFormInitialize checkoutFormInitialize = paymentService.createPackagePayment(visitorUser, pkg, request.getRemoteAddr());
            System.out.println(checkoutFormInitialize.toString());

            if (!checkoutFormInitialize.getStatus().equals(Status.SUCCESS.getValue())){

                log.error("Checkout Form Initialize Error -> " + checkoutFormInitialize.getErrorMessage());
                redirectAttributes.addFlashAttribute("errormessage",
                        "Şu anda ödeme alma işlemini başlatamadık. Lütfen tekrar deneyiniz veya diğer ödeme yöntemlerini kullanınız. Sorun ile ilgili destek talebi açarsanız arkadaşlarımız kısa süre içinde yardımcı olacaktır.");
                return "redirect:/package/order/" + packageId;
            }
            visitorUserService.updateToken(visitorUser.getId(), checkoutFormInitialize.getToken());

            model.addAttribute("popularCategories", packageService.visitorPopularCategories());
            model.addAttribute("message", new VisitorMessageDTO());
            model.addAttribute("iyzicoscript", checkoutFormInitialize.getCheckoutFormContent());

            return "visitor-package-payment";
            //success ? new order , error
            //new order canceled ? para iade
        }catch (Exception e){
            if (e instanceof DetectedException){
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            }else {
                log.error("Visitor Controller packageorderpaymentpage Error -> "+e.getMessage());
                redirectAttributes.addFlashAttribute("errormessage",
                        "İşleminiz şu anda gerçekleştirilemedi. Lütfen daha sonra tekrar deneyin.");
            }
            return "redirect:/package/order/" + packageId;
        }
    }

    @PostMapping("/package/order/iyzico/callback")
    public String visitorIyzicoCallback(@RequestParam("token") String token,
                                 RedirectAttributes redirectAttributes) {
        try {
            CheckoutForm checkoutForm = paymentService.infoPayment(token, "");
            System.out.println(checkoutForm.toString());

            if(checkoutForm.getStatus().equals(Status.SUCCESS.getValue())
                    && checkoutForm.getPaymentStatus().equals("SUCCESS")){

                //package ve url al
                VisitorUser visitorUser = visitorUserService.findByToken(token);

                if (visitorUser == null){
                    throw new DetectedException("Ödeme alındı fakat sipariş verilemedi. Konu hakkında sayfa altındaki yer alan iletişim formundan bizimle iletişime geçiniz.");
                }

                Package pkg = packageService.findById(visitorUser.getPackageId());

                if (pkg == null || !pkg.isState()){
                    throw new DetectedException("Ödeme alındı fakat sipariş verilemedi. Konu hakkında sayfa altındaki yer alan iletişim formundan bizimle iletişime geçiniz.");
                }

                //order oluştur
                long orderId = orderService.createVisitorPackageOrderReturnOrderId(pkg, visitorUser.getUrl());

                if (orderId == -1){
                    throw new DetectedException("Ödeme alındı fakat sipariş verilemedi. Konu hakkında sayfa altındaki yer alan iletişim formundan bizimle iletişime geçiniz.");
                }

                visitorUserService.resetToken(visitorUser.getId());

                //mail at
                mailService.asyncSendVisitorOrderMail(visitorUser.getEmail(), String.valueOf(orderId), visitorUser.getName(), visitorUser.getSurname());

                //takip sağla
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarılı ! Sipariş takip numaranız : <strong>" + orderId + "</strong> Sipariş takip sayfasından siparişinizin durumunu takip edebilirsiniz.");
            }else {
                if(checkoutForm.getErrorMessage()!=null)
                    redirectAttributes.addFlashAttribute("errormessage", checkoutForm.getErrorMessage());
                else
                    redirectAttributes.addFlashAttribute("errormessage", "İşlem başarısız !");
            }
        }catch (Exception e){
            if (e instanceof DetectedException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else{
                log.error("Visitor Controller visitorIyzicoCallback Error => " + e.getMessage());
                redirectAttributes.addFlashAttribute("errormessage", "İşleminiz yapılamadı, ödemeniz alındı ise iade için sayfanın altında yer alan iletişim bilgilerimizden bize ulaşabilirsiniz.");
            }
        }finally {
            return "redirect:/all-packages";
        }
    }

    @GetMapping("/package/order/status")
    public String orderStatusPage(Model model){
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        return "visitor-order-status";
    }

    @PostMapping("/package/order/status")
    public String orderStatus(RedirectAttributes redirectAttributes,
                              @RequestParam("orderId") String orderId,
                              Model model){
        try {
            VisitorPageOrderDTO visitorPageOrder = orderService.getVisitorOrderById(orderId);
            model.addAttribute("order", visitorPageOrder);
            model.addAttribute("message", new VisitorMessageDTO());
            model.addAttribute("popularCategories", packageService.visitorPopularCategories());
            return "visitor-order-status";
        }catch (Exception e) {
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            return "redirect:/package/order/status";
        }
    }


    @PostMapping("/message")        //footer visitor message
    public String visitorMessage(RedirectAttributes redirectAttributes,
                                 @ModelAttribute VisitorMessageDTO visitorMessage,
                                 HttpServletRequest httpServletRequest){
        try {
            boolean res = visitorMessageService.sendVisitorMessage(visitorMessage, httpServletRequest.getRemoteAddr());
            if (res)
                redirectAttributes.addFlashAttribute("successmessage", "Mesajınız başarıyla gönderildi. En kısa zamanda dönüş yapılacaktır.");
            else
                redirectAttributes.addFlashAttribute("errormessage", "Mesajınız şu anda gönderilemedi. Lütfen daha sonra tekrar deneyin.");
        }catch (Exception e){
            if (e instanceof DetectedException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else {
                log.error("Visitor Message Controller Error -> " + e.getMessage());
                redirectAttributes.addFlashAttribute("errormessage", "Mesajınız şu anda gönderilemedi. Lütfen daha sonra tekrar deneyin.");
            }
        }
        return "redirect:/";
    }

    @GetMapping("/kullanim-kosullari")       //terms use page
    public String termOfUse(Model model){
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        return "visitor-terms-of-use";
    }

    @GetMapping("/iade-ve-iptal")       //terms use page
    public String returnAndCancellation(Model model){
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        return "visitor-return-and-cancelled";
    }

}
