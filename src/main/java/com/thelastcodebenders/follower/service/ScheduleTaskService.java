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
    private DrawService drawService;

    public ScheduleTaskService(ApiService apiService,
                               MailService mailService,
                               UserService userService,
                               OrderService orderService,
                               DrawService drawService){
        this.apiService = apiService;
        this.mailService = mailService;
        this.userService = userService;
        this.orderService = orderService;
        this.drawService = drawService;
    }

    @Scheduled(fixedDelay = 40 * 60 * 1000, initialDelay = 50 * 1000)     //45 minutes
    public void servicesUpdate() {
        User admin = userService.getAdmin();

        String otherUpdateMessage = apiService.allApiUpdateOtherService();
        if (!otherUpdateMessage.equals(""))
            mailService.sendUpdateServiceStateMail(admin.getMail(), otherUpdateMessage);
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 10 * 1000)
    public void updateActiveOrderStatus(){
        //All Orders Update !
        orderService.updateActiveOrderStatus();

        //All Draw Orders Update !
        drawService.updateActiveOrderStatus();

        User admin = userService.getAdmin();

        //All Active Service Update !
        String activeUpdateMessage = apiService.allApiUpdateActiveService();

        if (activeUpdateMessage.equals("")){
            log.info("Tüm aktif servisler başarılı şekilde güncellendi ! Bir değişiklik görülmedi.");
        }else {
            log.info("Değişiklik tespit edilen servislerle ilgili admine mail gönderildi.");
            mailService.sendUpdateServiceStateMail(admin.getMail(), activeUpdateMessage);
        }
    }
}
