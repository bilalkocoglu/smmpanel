package com.thelastcodebenders.follower.configuration.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private CacheManager cacheManager;

    public CacheService (CacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    //service-xxxx CLEAR !
    public void servicesUpdate(){
        cacheManager.getCacheNames().forEach(cacheName ->{
            if (cacheName.split("-")[0].equals("service"))
                cacheManager.getCache(cacheName).clear();
        });
    }

    //packages-top-12
    public void topPackagesClear(){
        cacheManager.getCache("packages-top-12").clear();
    }



    /*
    ----------------------------------CACHE MAP-----------------------------------------
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
     */
}
