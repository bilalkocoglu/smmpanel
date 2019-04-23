package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleTaskService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleTaskService.class);

    private ApiService apiService;
    private MailService mailService;
    private UserService userService;
    private OrderService orderService;

    public ScheduleTaskService(ApiService apiService,
                               MailService mailService,
                               UserService userService,
                               OrderService orderService){
        this.apiService = apiService;
        this.mailService = mailService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @Scheduled(fixedDelay = 27 * 60 * 1000, initialDelay = 7 * 60 * 1000)     //45 minutes
    public void servicesUpdate() {
        String message = apiService.allApiUpdateService();

        if (message.equals(""))
            message = "Servisler başarılı şekilde güncellendi, servislerde bir değişiklik görülmedi.";

        User admin = userService.getAdmin();

        mailService.sendUpdateServiceStateMail(admin.getMail(), message);
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 10 * 1000)
    public void updateActiveOrderStatus(){
        orderService.updateActiveOrderStatus();
    }
}
