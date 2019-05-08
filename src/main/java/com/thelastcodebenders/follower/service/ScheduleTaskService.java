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

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 10 * 1000)     //45 minutes
    public void servicesUpdate() {
        //long startTime = System.nanoTime();
        String message = apiService.allApiUpdateService();

        if (!message.equals("")){
            User admin = userService.getAdmin();

            mailService.sendUpdateServiceStateMail(admin.getMail(), message);
        }else {
            //log.info("Servisler başarılı şekilde güncellendi ! Değişiklik görülmedi !");
        }

        //long endTime = System.nanoTime();
        //long totalTime = endTime - startTime;
        //System.out.println("Total Time => " + totalTime);
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 10 * 1000)
    public void updateActiveOrderStatus(){
        //log.info("All Order Update !");
        orderService.updateActiveOrderStatus();
    }
}
