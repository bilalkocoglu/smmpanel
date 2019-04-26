package com.thelastcodebenders.follower.controller;

import com.thelastcodebenders.follower.dto.RegisterFormDTO;
import com.thelastcodebenders.follower.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VisitorController {
    private static final Logger log = LoggerFactory.getLogger(VisitorController.class);

    private UserService userService;

    public VisitorController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model){
        return "visitor-index";
    }

    @GetMapping("/login")
    public String loginPage(Model model){
        return "visitor-login";
    }

    @GetMapping("/login-failure")
    public String loginFailure(RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errormessage", "Kullanıcı adı veya şifre hatalı !");
        return "redirect:/login";
    }

    @GetMapping("/registration")
    public String registrationPage(Model model){
        model.addAttribute("userform", new RegisterFormDTO());
        return "visitor-register";
    }

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
}
