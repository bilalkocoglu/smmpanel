package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.client.telegram.TelegramService;
import com.thelastcodebenders.follower.configuration.cache.CacheService;
import com.thelastcodebenders.follower.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduleTaskService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleTaskService.class);

    private ApiService apiService;
    private OrderService orderService;
    private DrawService drawService;
    private TelegramService telegramService;
    private CacheService cacheService;
    private PackageService packageService;

    public ScheduleTaskService(ApiService apiService,
                               OrderService orderService,
                               DrawService drawService,
                               TelegramService telegramService,
                               CacheService cacheService,
                               PackageService packageService){
        this.apiService = apiService;
        this.orderService = orderService;
        this.drawService = drawService;
        this.telegramService = telegramService;
        this.cacheService = cacheService;
        this.packageService = packageService;
    }

    @Scheduled(fixedDelay = 40 * 60 * 1000, initialDelay = 50 * 1000)     //45 minutes
    public void servicesUpdate() {
        //User admin = userService.getAdmin();

        List<String> messages = apiService.allApiUpdateOtherService();
        for (String msg : messages) {
            telegramService.sendAdminMessage(msg);
        }
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 10 * 1000)
    public void updateActiveOrderStatus(){
        //All Orders Update !
        orderService.updateActiveOrderStatus();

        //All Draw Orders Update !
        drawService.updateActiveOrderStatus();

        //User admin = userService.getAdmin();

        //All Active Service Update !
        String activeUpdateMessage = apiService.allApiUpdateActiveService();

        if (activeUpdateMessage.equals("")){
            log.info("Tüm aktif servisler başarılı şekilde güncellendi ! Bir değişiklik görülmedi.");
        }else {
            log.info("Değişiklik tespit edilen servislerle ilgili admine mail gönderildi.");
            //mailService.sendUpdateServiceStateMail(admin.getMail(), activeUpdateMessage);
            telegramService.sendAdminMessage(activeUpdateMessage);
        }
    }


    @Scheduled(fixedDelay = 30 * 60 * 1000, initialDelay = 30 * 60 * 1000)
    public void clearCache(){
        cacheService.topPackagesClear();
        packageService.activePackagesTop12();
    }

}
