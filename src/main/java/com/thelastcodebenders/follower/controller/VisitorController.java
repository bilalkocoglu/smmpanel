package com.thelastcodebenders.follower.controller;

import com.thelastcodebenders.follower.blog.enums.FindSlugType;
import com.thelastcodebenders.follower.blog.model.Post;
import com.thelastcodebenders.follower.blog.service.PostService;
import com.thelastcodebenders.follower.enums.RoleType;
import com.thelastcodebenders.follower.payment.paytr.PaytrService;
import com.thelastcodebenders.follower.payment.paytr.dto.CallbackRequest;
import com.thelastcodebenders.follower.payment.paytr.dto.TokenResponse;
import com.thelastcodebenders.follower.seo.SitemapView;
import com.thelastcodebenders.follower.dto.*;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.payment.iyzipay.IyzicoService;
import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.model.VisitorUser;
import com.thelastcodebenders.follower.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class VisitorController {
    private static final Logger log = LoggerFactory.getLogger(VisitorController.class);

    private UserService userService;
    private PackageService packageService;
    private CategoryService categoryService;
    private VisitorMessageService visitorMessageService;
    private CategoryArticleService categoryArticleService;
    private IyzicoService iyzicoService;
    private VisitorUserService visitorUserService;
    private OrderService orderService;
    private ServiceService serviceService;
    private final SitemapView sitemapView;
    private PaytrService paytrService;
    private PostService postService;

    public VisitorController(UserService userService,
                             PackageService packageService,
                             CategoryService categoryService,
                             VisitorMessageService visitorMessageService,
                             CategoryArticleService categoryArticleService,
                             IyzicoService iyzicoService,
                             VisitorUserService visitorUserService,
                             OrderService orderService,
                             ServiceService serviceService,
                             SitemapView sitemapView,
                             PaytrService paytrService,
                             PostService postService){
        this.userService = userService;
        this.packageService = packageService;
        this.categoryService = categoryService;
        this.visitorMessageService = visitorMessageService;
        this.categoryArticleService = categoryArticleService;
        this.iyzicoService = iyzicoService;
        this.visitorUserService = visitorUserService;
        this.orderService = orderService;
        this.serviceService = serviceService;
        this.sitemapView = sitemapView;
        this.paytrService = paytrService;
        this.postService = postService;
    }

    //Login
    @GetMapping("/login")
    public String loginPage(Model model){
        RoleType type = userService.getAuthUserRole();

        if (type == RoleType.ADMIN){
            return "redirect:/admin/dashboard";
        }else if (type == RoleType.USER){
            return "redirect:/user/dashboard";
        }else{
            model.addAttribute("title", "Giriş Yap - İnstagram Takipçi   Facebook | Twitter | Sosyal Trend");
            model.addAttribute("description", "SMM panel ile sosyal medyadaki tüm hesaplarınıza uygun fiyatlı ve kaliteli servislerimiz ile beğeni, takipçi vb. aklınıza gelebilecek herşeyi gönderebilirsiniz. Hemen SMM panelimize giriş yaparak Sosyal Medya Panelinde yeni trend ile tanışın. Hızlı ve garantili servisin tadını çıkarın");
            model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");
            return "visitor-login";
        }
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
        model.addAttribute("title", "Şifremi Unuttum - İnstagram Takipçi   Facebook | Twitter | Sosyal Trend");
        model.addAttribute("description", "SMM panel ile sosyal medyadaki tüm hesaplarınıza uygun fiyatlı ve kaliteli servislerimiz ile beğeni, takipçi vb. aklınıza gelebilecek herşeyi gönderebilirsiniz. Hemen SMM panelimize giriş yaparak Sosyal Medya Panelinde yeni trend ile tanışın. Hızlı ve garantili servisin tadını çıkarın");
        model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");

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
        model.addAttribute("title", "Hesap Aktivasyon - İnstagram Takipçi   Facebook | Twitter | Sosyal Trend");
        model.addAttribute("description", "SMM panel ile sosyal medyadaki tüm hesaplarınıza uygun fiyatlı ve kaliteli servislerimiz ile beğeni, takipçi vb. aklınıza gelebilecek herşeyi gönderebilirsiniz. Hemen SMM panelimize giriş yaparak Sosyal Medya Panelinde yeni trend ile tanışın. Hızlı ve garantili servisin tadını çıkarın");
        model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");

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
        model.addAttribute("title", "Kayıt Ol - İnstagram Takipçi   Facebook | Twitter | Sosyal Trend");
        model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");
        model.addAttribute("description", "SMM panelimizin tüm özelliklerinden yararlanmak ve uygun fiyatlı servislerden faydalanmak için hemen kaydolun. Sosyal Trend olarak sizlere hızlı ve kaliteli hizmeti sunuyoruz. Kayıt olmadan da sipariş verebileceğiniz sistemimizde kayıt olarak fiyat avantajından yararlanabilirsiniz.");

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
    @GetMapping("/account-activate/{code}")     //SEND TELEGRAM MESSAGE
    public String accountActivateWithMail(@PathVariable("code") String secretKey,
                                          RedirectAttributes redirectAttributes){
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
        model.addAttribute("title", "Sosyal Medya Paneli - SMM Panel - Sosyal Trend - Yeni Trendiniz.");
        model.addAttribute("description", "SMM Panel'de yeni trend. Sosyal medya panelimiz ile instagram, twitter, facebook gibi sosyal medyalardan uygun fiyatlı takipçi, beğeni gibi hizmet satınalabilirsiniz. Hızlı ve kaliteli hizmet ile şifresiz gönderim imkanı sağlıyoruz.");
        model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");
        return "visitor-index";
    }

    //All Packages
    @GetMapping("/all-packages")
    public String allPackages(Model model){
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        model.addAttribute("categories", packageService.visitorAllPackageCategories());
        model.addAttribute("title", "Tüm Paketler - İnstagram | Facebook | Twitter | Sosyal Medya Satış Paneli");
        model.addAttribute("description", "İnstagram takipçi, twitter takipçi gibi bir çok hizmeti sitemizden şifresiz bir şekilde satınalabilirsiniz. Sosyal medya panelinde yeni trend. SMM Panel denince akla gelen ilk adres olan Sosyal Trend'e hemen sizde kaydolun ve fırsatları kaçırmayın.");
        model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");
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
            model.addAttribute("title",  category.getName() + " Takipçi Satın al - " + category.getName() + " Beğeni| Sosyal Trend");
            model.addAttribute("description", category.getName() + " takipçi satın al, " + category.getName() + " beğeni hilesi ve " + category.getName() + " takipçi hilesi gibi sitelerde vakit kaybetmeden uygun fiyatlı bir şekilde " + category.getName() + " begeni, " + category.getName()+ " takipci satın alarak " + category.getName() + " profilinizi güçlendirebilirsiniz. Hemen sende sitemizi ziyaret et ve şifreni vermeden takipçi almanın keyfini çıkar.");
            model.addAttribute("keywords", category.getName() + " takipçi hilesi, " + category.getName()+" takipçi, "+category.getName()+" beğeni hilesi, "+ category.getName() +" beğeni, takipçi hilesi, " +category.getName()+ " takipçi satın al, "+category.getName()+" beğeni satın al, smm panel");

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

        double apiBalance;
        if (pkg.getService().getApi().isUseUSD())
            apiBalance = pkg.getService().getApi().getBalance() * pkg.getService().getApi().getRateUSD();
        else
            apiBalance = pkg.getService().getApi().getBalance();
        //System.out.println(apiPrice);
        //System.out.println(pkg.getService().getApi().getBalance());
        if ( apiBalance < apiPrice){
            log.error("Api bakiyesi yetersiz !");
            redirectAttributes.addFlashAttribute("errormessage", "İstediğiniz paket için geçici bir süre sipariş alamıyoruz. Lütfen daha sonra tekrar deneyin.");
            return "redirect:/all-packages";
        }

        model.addAttribute("pkg", packageService.createUserPageServiceFormat(packageId));
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("title",  pkg.getCategory().getName() + " Takipçi Satın al - " + pkg.getCategory().getName() + " Beğeni| Sosyal Trend");
        model.addAttribute("description", pkg.getCategory().getName() + " takipçi satın al, " + pkg.getCategory().getName() + " beğeni hilesi ve " + pkg.getCategory().getName() + " takipçi hilesi gibi sitelerde vakit kaybetmeden uygun fiyatlı bir şekilde " + pkg.getCategory().getName() + " begeni, " + pkg.getCategory().getName()+ " takipci satın alarak " + pkg.getCategory().getName() + " profilinizi güçlendirebilirsiniz. Hemen sende sitemizi ziyaret et ve şifreni vermeden takipçi almanın keyfini çıkar.");
        model.addAttribute("keywords", pkg.getCategory().getName() + " takipçi hilesi, " + pkg.getCategory().getName()+" takipçi, "+pkg.getCategory().getName()+" beğeni hilesi, "+ pkg.getCategory().getName() +" beğeni, takipçi hilesi, " +pkg.getCategory().getName()+ " takipçi satın al, "+pkg.getCategory().getName()+" beğeni satın al, smm panel");

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
            VisitorUser visitorUser = visitorUserService.save(packageOrderPaymentForm, pkg);  //visitor user status ekle

            /*
            //create iyzipay payment page
            CheckoutFormInitialize checkoutFormInitialize = iyzicoService.createPackagePayment(visitorUser, pkg, request.getRemoteAddr());
            System.out.println(checkoutFormInitialize.toString());

            if (!checkoutFormInitialize.getStatus().equals(Status.SUCCESS.getValue())){

                log.error("Checkout Form Initialize Error -> " + checkoutFormInitialize.getErrorMessage());
                redirectAttributes.addFlashAttribute("errormessage",
                        "Şu anda ödeme alma işlemini başlatamadık. Lütfen tekrar deneyiniz veya diğer ödeme yöntemlerini kullanınız. Sorun ile ilgili destek talebi açarsanız arkadaşlarımız kısa süre içinde yardımcı olacaktır.");
                return "redirect:/package/order/" + packageId;
            }
            visitorUserService.update(visitorUser.getId(), checkoutFormInitialize.getToken());
             */
            TokenResponse tokenResponse = paytrService.visitorCreateToken(visitorUser, request.getHeader("x-forwarded-for"), pkg);

            if (tokenResponse.getStatus().equals("success")){
                //System.out.println("token = " + tokenResponse.getToken());
                //card payment (token ile birlikte tutulacak)
                visitorUser.setToken(tokenResponse.getMerchant_oid());
                boolean res = visitorUserService.update(visitorUser);
                if (!res){
                    throw new DetectedException("Ödeme işlemi başlatılamadı. Lütfen daha sonra tekrar deneyin.");
                }
            }else {
                throw new DetectedException("Ödeme işlemi başlatılamadı. Lütfen daha sonra tekrar deneyin.");
            }

            model.addAttribute("popularCategories", packageService.visitorPopularCategories());
            model.addAttribute("message", new VisitorMessageDTO());
            //model.addAttribute("iyzicoscript", checkoutFormInitialize.getCheckoutFormContent());
            model.addAttribute("paytrtoken", tokenResponse.getToken());
            model.addAttribute("title", "Sosyal Medya Paneli - SMM Panel - Sosyal Trend - Yeni Trendiniz.");
            model.addAttribute("description", "SMM Panel'de yeni trend. Sosyal medya panelimiz ile instagram, twitter, facebook gibi sosyal medyalardan uygun fiyatlı takipçi, beğeni gibi hizmet satınalabilirsiniz. Hızlı ve kaliteli hizmet ile şifresiz gönderim imkanı sağlıyoruz.");
            model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");

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
/*
    @PostMapping("/package/order/iyzico/callback")
    public String visitorIyzicoCallback(@RequestParam("token") String token,
                                 RedirectAttributes redirectAttributes) {
        try {
            CheckoutForm checkoutForm = iyzicoService.infoPayment(token, "");
            System.out.println(checkoutForm.toString());

            if(checkoutForm.getStatus().equals(Status.SUCCESS.getValue())
                    && checkoutForm.getPaymentStatus().equals("SUCCESS")){

                //package ve url al
                VisitorUser visitorUser = visitorUserService.findActiveByToken(token);

                if (visitorUser == null){
                    telegramService.asyncSendAdminMessage("Ziyaretçi bir kullanıcıdan ödeme alındı fakat token eşleşmemesi sebebiyle sipariş verilemedi.");
                    throw new DetectedException("Ödeme alındı fakat sipariş verilemedi. Konu hakkında sayfa altındaki yer alan iletişim formundan bizimle iletişime geçiniz.");
                }

                Package pkg = packageService.findById(visitorUser.getPackageId());

                if (pkg == null || !pkg.isState()){
                    telegramService.asyncSendAdminMessage(visitorUser.getEmail() + " ziyaretçisinden ödeme alındı fakat paket durumundaki değişimden dolayı sipariş verilemedi.");
                    throw new DetectedException("Ödeme alındı fakat sipariş verilemedi. Konu hakkında sayfa altındaki yer alan iletişim formundan bizimle iletişime geçiniz.");
                }

                //order oluştur
                long orderId = orderService.createVisitorPackageOrderReturnOrderId(pkg, visitorUser.getUrl());

                if (orderId == -1){
                    telegramService.asyncSendAdminMessage(visitorUser.getEmail() + " ziyaretçisinden bir kullanıcıdan ödeme alındı fakat " + pkg.getService().getApi().getName() + " API'den " + pkg.getService().getId() + " idli servis ile ilgili cevap gelmediği için sipariş verilemedi.");
                    throw new DetectedException("Ödeme alındı fakat sipariş verilemedi. Konu hakkında sayfa altındaki yer alan iletişim formundan bizimle iletişime geçiniz.");
                }

                visitorUserService.resetToken(visitorUser.getId());

                //mail at
                mailService.asyncSendVisitorOrderMail(visitorUser.getEmail(), String.valueOf(orderId), visitorUser.getName(), visitorUser.getSurname());

                //takip sağla
                telegramService.asyncSendAdminMessage(visitorUser.getEmail() + " ziyaretçisinden " + pkg.getPrice() + " tutarında ödeme alındı ve " + pkg.getId() + "paketinden sipariş verildi.");
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

 */

    @PostMapping("/paytr/callback")
    public void visitorPaytrCallback(@ModelAttribute CallbackRequest callbackRequest,
                                       HttpServletResponse httpServletResponse) throws IOException{
        System.out.println("PayTR Callback !");
        System.out.println(callbackRequest.toString());
        try {
            paytrService.callbackAction(callbackRequest);
        }catch (Exception e){
            log.error("PayTR Callback Error -> " + e.getMessage());
        }

        httpServletResponse.getWriter().write("OK");
    }


    //Order Status Page
    @GetMapping("/order/status")
    public String orderStatusPage(Model model){
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        model.addAttribute("title", "Sipariş Takip - İnstagram | Facebook | Twitter | Sosyal Medya Satış Paneli");
        model.addAttribute("description", "Sipariş takip sistemimiz ile kayıt olmadan yapılan işlemlerinizi mailinize gelen takip numarası ile kolay bir şekilde takip edebilirsiniz. Kayıt olmadan ve şifre vermeden SMM panelimizden sipariş vermeye başlayabilirsiniz. Sosyal Medya Panelinde yeni Trend.");
        model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");

        return "visitor-order-status";
    }

    @PostMapping("/order/status")
    public String orderStatus(RedirectAttributes redirectAttributes,
                              @RequestParam("orderId") String orderId,
                              Model model){
        try {
            VisitorPageOrderDTO visitorPageOrder = orderService.getVisitorOrderById(orderId);
            model.addAttribute("order", visitorPageOrder);
            model.addAttribute("message", new VisitorMessageDTO());
            model.addAttribute("popularCategories", packageService.visitorPopularCategories());
            model.addAttribute("title", "Sipariş Takip - İnstagram | Facebook | Twitter | Sosyal Medya Satış Paneli");
            model.addAttribute("description", "Kullanım koşulları sizin ve bizim güvenliğini sağlamak amacı ile hazırlanmıştır. Sosyal medya panelimizden yapılan tüm işlemlerde kullanım koşullarını kabul etmiş sayılırsınız. Koşullara uymayan tüm siparişler ve işlemler itiraz kabul edilmeksizin iptal edilmektedir.");
            model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");

            return "visitor-order-status";
        }catch (Exception e) {
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            return "redirect:/order/status";
        }
    }


    @PostMapping("/message")        //footer visitor message
    public String visitorMessage(RedirectAttributes redirectAttributes,
                                 @ModelAttribute VisitorMessageDTO visitorMessage,
                                 HttpServletRequest httpServletRequest){
        try {
            boolean res = visitorMessageService.sendVisitorMessage(visitorMessage, httpServletRequest.getHeader("x-forwarded-for"));
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
        model.addAttribute("title", "Kullanım Koşulları - İnstagram | Facebook | Twitter | Sosyal Medya Satış Paneli");
        model.addAttribute("description", "Kullanım koşulları sizin ve bizim güvenliğini sağlamak amacı ile hazırlanmıştır. Sosyal medya panelimizden yapılan tüm işlemlerde kullanım koşullarını kabul etmiş sayılırsınız. Koşullara uymayan tüm siparişler ve işlemler itiraz kabul edilmeksizin iptal edilmektedir.");
        model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");
        return "visitor-terms-of-use";
    }

    @GetMapping("/iade-ve-iptal")       //terms use page
    public String returnAndCancellation(Model model){
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        model.addAttribute("title", "İptal ve İade - İnstagram | Facebook | Twitter | Sosyal Medya Satış Paneli");
        model.addAttribute("description", "İptal ve iade koşullarımız ile sizleri garanti altına alıyoruz. SMM panelimizden satın aldığınız tüm hizmetlede bu koşulları kabul etmiş sayılırsınız. Sosyal medya panelinden yaptığınız tüm işlemler kullanım koşullarını kabul ettiğinizi gösterir. Hemen sitemizi ziyaret ederek sosyal medya paketlerimize göz atabilirsiniz");
        model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");
        return "visitor-return-and-cancelled";
    }

    @GetMapping("/fiyat-listesi")
    public String servicePriceList(Model model){
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        model.addAttribute("listItems", serviceService.createVisitorServicesItems());
        model.addAttribute("title", "Fiyat Listesi - İnstagram | Facebook | Twitter | Sosyal Medya Satış Paneli");
        model.addAttribute("description", "İnstagram takipçi, twitter takipçi gibi bir çok hizmeti sitemizden şifresiz bir şekilde satınalabilirsiniz. Sosyal medya panelinde yeni trend. SMM Panel denince akla gelen ilk adres olan Sosyal Trend'e hemen sizde kaydolun ve fırsatları kaçırmayın.");
        model.addAttribute("keywords", "smm panel, sosyal medya paneli, instagram takipçi hilesi, instagram beğeni hilesi, facebook beğeni hilesi,  twitter takipçi,  twitter beğeni hilesi, youtube abone kasma");
        return "visitor-service-list";
    }

    @RequestMapping(path = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public SitemapView create(){
        return sitemapView;
    }

    @RequestMapping(value = "/robots.txt")
    public void robots(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.getWriter().write("User-agent: *\n" +
                    "Allow:\n"+
                    "Allow:/\n"+
                    "Disallow: /cgi-bin/\n"+
                    "Disallow: /login-failure\n"+
                    "Disallow: /forgot-password\n"+
                    "Disallow: /account-activate\n"+
                    "Disallow: /package/order\n"+
                    "Sitemap: https://sosyaltrend.net/sitemap.xml");
        } catch (IOException e) {
            log.info("robots(): "+e.getMessage());
        }
    }

    /*
    @GetMapping(path = "/deneme", produces = MediaType.APPLICATION_XML_VALUE)
    public void deneme(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.getWriter().write(userService.ddeneme());
    }

     */

    //BLOG
    @GetMapping("/blog")
    public String blogPage(Model model,
                           @RequestParam(value = "page", required = false, defaultValue = "1") int page){
        try {
            List<Integer> pageNumbers = postService.getPageNumbers();
            model.addAttribute("postCategories", postService.postCategories());
            model.addAttribute("pagination", pageNumbers);
            page = postService.pageNumberControl(page, pageNumbers.get(pageNumbers.size()-1));
            model.addAttribute("activePage", page);
            model.addAttribute("posts", postService.getPosts(page));
            model.addAttribute("hotnews", postService.getHotNews());
            model.addAttribute("populars", postService.getPopulars());
            model.addAttribute("categoryPage", false);



            model.addAttribute("popularCategories", packageService.visitorPopularCategories());
            model.addAttribute("message", new VisitorMessageDTO());
            model.addAttribute("title", "Blog");
            model.addAttribute("description", "Blog");
            model.addAttribute("keywords", "Blog");
            return "visitor-blog";
        }catch (Exception e){
            log.error("VisitorController blogPage Error -> " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/blog/{postSlug}")
    public String postPage(Model model,
                           RedirectAttributes redirectAttributes,
                           @PathVariable("postSlug") String postSlug){
        try {
            Post post = postService.findPostBySlug(postSlug);
            model.addAttribute("post", post);
            postService.postViewCountPlus(post);
            model.addAttribute("commentCount", 0);
            model.addAttribute("postCategories", postService.postCategories());
            model.addAttribute("hotnews", postService.getHotNews());
            model.addAttribute("populars", postService.getPopulars());
            model.addAttribute("previewblog", false);
            model.addAttribute("similarPosts", postService.getSimilarPosts(post));

            model.addAttribute("popularCategories", packageService.visitorPopularCategories());
            model.addAttribute("message", new VisitorMessageDTO());
            model.addAttribute("title", "Blog");
            model.addAttribute("description", "Blog");
            model.addAttribute("keywords", "Blog");

            return "visitor-blog-post";
        }catch (Exception e){
            if (e instanceof DetectedException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else {
                log.error("VisitorController postPage Error -> " + e.getMessage());
                redirectAttributes.addFlashAttribute("errormessage", "Malesef yazı bulunamadı!");
            }
            return "redirect:/blog";
        }
    }

    @GetMapping("/blog/{postSlug}/next")
    public String nextPostPage(RedirectAttributes redirectAttributes,
                               @PathVariable("postSlug") String postSlug){
        try {
            String nextSlug = postService.nextPrevPostSlug(postSlug, FindSlugType.NEXT);
            return "redirect:/blog/" + nextSlug;
        }catch (Exception e){
            return "redirect:/blog/" + postSlug;
        }
    }

    @GetMapping("/blog/{postSlug}/prev")
    public String prevPostPage(RedirectAttributes redirectAttributes,
                               @PathVariable("postSlug") String postSlug){
        try {
            String prevSlug = postService.nextPrevPostSlug(postSlug, FindSlugType.PREV);
            return "redirect:/blog/" + prevSlug;
        }catch (Exception e){
            return "redirect:/blog/" + postSlug;
        }
    }

    @GetMapping("/blog/category/{categoryName}")
    public String categoryPage(Model model,
                               RedirectAttributes redirectAttributes,
                               @PathVariable("categoryName") String categoryName,
                               @RequestParam(value = "page", required = false, defaultValue = "1") int page){
        try {
            Category category = categoryService.findCategoryByName(categoryName);

            if (category == null)
                throw new DetectedException("Böyle bir kategori bulunamadı !");
            model.addAttribute("currentcategory", category);
            List<Integer> pageNumbers = postService.getCategoryPageNumbers(category);
            model.addAttribute("pagination", pageNumbers);
            model.addAttribute("postCategories", postService.postCategories());
            page = postService.pageNumberControl(page, pageNumbers.get(pageNumbers.size()-1));
            model.addAttribute("activePage", page);
            model.addAttribute("posts", postService.categorPosts(category, page));
            model.addAttribute("hotnews", postService.getHotNews());
            model.addAttribute("populars", postService.getPopulars());
            model.addAttribute("categoryPage", true);


            model.addAttribute("popularCategories", packageService.visitorPopularCategories());
            model.addAttribute("message", new VisitorMessageDTO());
            model.addAttribute("title", "Blog");
            model.addAttribute("description", "Blog");
            model.addAttribute("keywords", "Blog");

            return "visitor-blog";

        }catch (Exception e){
            if (e instanceof DetectedException)
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            else {
                log.error("VisitorController categoryPage Error -> " + e.getMessage());
                redirectAttributes.addFlashAttribute("errormessage", "Böyle bir kategori bulunamadı.");
            }
            return "redirect:/blog";
        }
    }
}
