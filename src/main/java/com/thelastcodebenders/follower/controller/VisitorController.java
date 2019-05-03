package com.thelastcodebenders.follower.controller;

import com.thelastcodebenders.follower.dto.RegisterFormDTO;
import com.thelastcodebenders.follower.dto.VisitorMessageDTO;
import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.service.CategoryService;
import com.thelastcodebenders.follower.service.PackageService;
import com.thelastcodebenders.follower.service.UserService;
import com.thelastcodebenders.follower.service.VisitorMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
public class VisitorController {
    private static final Logger log = LoggerFactory.getLogger(VisitorController.class);

    private UserService userService;
    private PackageService packageService;
    private CategoryService categoryService;
    private VisitorMessageService visitorMessageService;

    public VisitorController(UserService userService,
                             PackageService packageService,
                             CategoryService categoryService,
                             VisitorMessageService visitorMessageService){
        this.userService = userService;
        this.packageService = packageService;
        this.categoryService = categoryService;
        this.visitorMessageService = visitorMessageService;
    }

    //Login
    @GetMapping("/login")
    public String loginPage(Model model){
        return "visitor-login";
    }

    //AUTH Failure
    @GetMapping("/login-failure")
    public String loginFailure(RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errormessage", "Kullanıcı adı veya şifre hatalı !");
        return "redirect:/login";
    }

    //Register Page
    @GetMapping("/registration")
    public String registrationPage(Model model){
        model.addAttribute("userform", new RegisterFormDTO());
        return "visitor-register";
    }

    //Create User
    @PostMapping("/registration")
    public String createUser(@ModelAttribute RegisterFormDTO registerFormDTO, RedirectAttributes redirectAttributes){
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
            if (e instanceof RuntimeException)
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
            if (e instanceof RuntimeException){
                redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
                return "redirect:/login";
            }else
                return "redirect:/";
        }
    }


    //Index
    @GetMapping("/")
    public String home(Model model){
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
    @GetMapping("/package/{categoryUrlName}")
    public String packagesByCategory(@PathVariable("categoryUrlName") String categoryUrlName,
                                     Model model,
                                     RedirectAttributes redirectAttributes){
        try {
            Category category = categoryService.visitorPackagePageCategory(categoryUrlName);
            if (packageService.isValidatePackageCategory(category))
                model.addAttribute("packageItems", packageService.createVisitorPackagesFormat(category));

            model.addAttribute("category", category);
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
            return "redirect:/all-packages";
        }
        model.addAttribute("message", new VisitorMessageDTO());
        model.addAttribute("popularCategories", packageService.visitorPopularCategories());
        return "visitor-packages";
    }

    @PostMapping("/message")
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
            redirectAttributes.addFlashAttribute("errormessage", e.getMessage());
        }
        return "redirect:/";
    }
}
