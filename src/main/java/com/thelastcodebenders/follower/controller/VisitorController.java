package com.thelastcodebenders.follower.controller;

import com.thelastcodebenders.follower.dto.RegisterFormDTO;
import com.thelastcodebenders.follower.service.MailService;
import com.thelastcodebenders.follower.service.RoleService;
import com.thelastcodebenders.follower.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VisitorController {
    private static final Logger log = LoggerFactory.getLogger(VisitorController.class);

    private RoleService roleService;
    private UserService userService;
    private MailService mailService;

    public VisitorController(RoleService roleService,
                             UserService userService,
                             MailService mailService){
        this.roleService = roleService;
        this.userService = userService;
        this.mailService = mailService;
    }

    @GetMapping("/")
    public String home(Model model){
        return "redirect:/login";
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

    @PostMapping("registration")
    public String createUser(@ModelAttribute RegisterFormDTO registerFormDTO, RedirectAttributes redirectAttributes){
        try {
            boolean res = userService.saveUser(registerFormDTO);
            if (res){
                redirectAttributes.addFlashAttribute("successmessage", "İşlem başarıyla gerçekleştirildi !");
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
}
