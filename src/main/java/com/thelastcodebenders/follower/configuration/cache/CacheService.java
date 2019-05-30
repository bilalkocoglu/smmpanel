package com.thelastcodebenders.follower.configuration.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CacheService {
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private CacheManager cacheManager;

    public CacheService (CacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    //service-xxxx CLEAR !
    public void servicesClear(){
        cacheManager.getCacheNames().forEach(cacheName ->{
            if (cacheName.split("-")[0].equals("service"))
                Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        });
        log.info("Service Caches Clear !");
    }

    //packages-top-12
    public void topPackagesClear(){
        Objects.requireNonNull(cacheManager.getCache("packages-top-12")).clear();
    }

    public void askedQuestionsClear(){
        Objects.requireNonNull(cacheManager.getCache("asked-questions")).clear();
        log.info("Askedquestions Cache Clear !");
    }

    public void announcementsClear(){
        Objects.requireNonNull(cacheManager.getCache("announcements")).clear();
        log.info("Announcements Cache Clear !");
    }

    public void bankaccountsClear(){
        Objects.requireNonNull(cacheManager.getCache("bankAccounts")).clear();
        log.info("BankAccounts Cache Clear !");
    }

    public void popularCategoryClear(){
        Objects.requireNonNull(cacheManager.getCache("popularCategories")).clear();
    }

    /*
    ----------------------------------CACHE MAP-----------------------------------------
        INIT METHODS (SocialManagementApplication.java)
        ServiceService.createVisitorServicesItems();
		ServiceService.createUserServicesItems();
		PackageService.activePackagesTop12();
		AskedQuestionService.allAskedQuestions();
        AnnouncementService.findAll();
        BankAccountService.allAccounts();

        **service-xxxx (cache name)
            -ServiceService.createVisitorServicesItems()    -> name: service-visitor
            -ServiceService.createUserServicesItems()   -> name: service-userpage

            CLEARS
                -AdminController.updateService()
                -ApiService.allApiUpdateActiveService()
                -ApiService.changeState()

        **packages-top-12 (cache name)
            -PackageService.activePackagesTop12()   -> name: packages-top-12

            CLEARS
                -30m Periods

        **asked-questions (cache name)
            -AskedQuestionService.findAll() -> name: asked-questions

            CLEARS
                -AskedQuestionService.save()
                -AskedQuestionService.delete()

        **announcements
            -AnnouncementService.findAll() -> name: announcements

            CLEARS
                -AnnouncementService.save()
                -AnnouncementService.delete()

        **bankAccounts
            -BankAccountService.allAccounts() -> name: allAccounts

            CLEARS
                -BankAccountService.save -delete

        **popularCategories
            -PackageService.visitorPopularCategories() -> name: popularCategories

            CLEARS
                -PackageService.save
                -PackageService.changeState
                -PackageService.servicePassivateHandler

     */
}
