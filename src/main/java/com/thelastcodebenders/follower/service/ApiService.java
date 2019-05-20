package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.client.panel.PanelService;
import com.thelastcodebenders.follower.client.rate.CurrencyRateService;
import com.thelastcodebenders.follower.dto.OtherServiceUpdateDTO;
import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.enums.UserAction;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.*;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.repository.APIRepository;
import com.thelastcodebenders.follower.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.*;

@org.springframework.stereotype.Service
public class ApiService {
    private static final Logger log  = LoggerFactory.getLogger(ApiService.class);

    private static final double API_BALANCE_LIMIT = 3;

    private APIRepository apiRepository;
    private PanelService panelService;
    private ServiceRepository serviceRepository;
    private PackageService packageService;
    private MailService mailService;
    private UserService userService;
    private DrawPrizeService drawPrizeService;
    private CurrencyRateService currencyRateService;

    public ApiService(APIRepository apiRepository,
                      PanelService panelService,
                      ServiceRepository serviceRepository,
                      PackageService packageService,
                      MailService mailService,
                      UserService userService,
                      DrawPrizeService drawPrizeService,
                      CurrencyRateService currencyRateService){
        this.apiRepository = apiRepository;
        this.panelService = panelService;
        this.serviceRepository = serviceRepository;
        this.packageService = packageService;
        this.mailService = mailService;
        this.userService = userService;
        this.drawPrizeService = drawPrizeService;
        this.currencyRateService = currencyRateService;
    }

    public List<String> adminTableColumns(){
        List<String> columns = new ArrayList<>();
        columns.add("Name");
        columns.add("Url");
        columns.add("State");
        columns.add("USD Rate");
        columns.add("Balance");
        return columns;
    }

    public List<String> apiTableColumns(){
        List<String> columns = new ArrayList<>();
        columns.add("Id");
        columns.add("Name");
        columns.add("Url");
        columns.add("Key");
        columns.add("State");
        columns.add("Balance");
        columns.add("Action");
        return columns;
    }



    public List<API> getAllAPIs(){
        return apiRepository.findAll();
    }


    private boolean isAlreadyApi(String apiUrl){
        List<API> apis = apiRepository.findByUrl(apiUrl);

        if (apis.isEmpty())
            return false;
        else
            return true;
    }


    public boolean save(API api){
        try {
            if (isAlreadyApi(api.getUrl()))
                throw new DetectedException("Eklemek istediğiniz Api ile aynı url e sahip başka bir Api mevcut !");

            api.setState(true);
            double balance = panelService.getBalance(api);
            if (balance == -1){
                throw new DetectedException("Eklemek istediğiniz api kullanıma uygun değil !");
            }

            //set currency rate
            if (api.isUseUSD()){
                api.setRateUSD(currencyRateService.getUSD());
            }

            List<Service> services = panelService.getAllServices(api);
            if (services == null || services.isEmpty()){
                throw new DetectedException("Eklemek istediğiniz api kullanıma uygun değil !");
            }

            log.info("Api Service Count : " + services.size());
//optimize
            /*
            int parseCount = 10;
            if (services.size()<parseCount)
                serviceRepository.saveAll(services);
            else {
                int repeatCount = services.size() / 20;
                if (services.size() % 20 != 0)
                    repeatCount++;

                for(int i=0; i < repeatCount; i++){
                    int startIndex = parseCount*i;
                    int endIndex;

                    if ((i+1) == repeatCount){
                        endIndex = services.size();
                    }else {
                        endIndex = (parseCount*(i+1));
                    }

                    serviceRepository.saveAll(services.subList(startIndex, endIndex));
                    log.info(startIndex + " - " + (endIndex-1) + " Service Saved !");
                }
            }

             */
            int counter = 1;
            for (Service service:services) {
                serviceRepository.save(service);
                log.info("Api Service => " +counter + " Saved !");
                counter++;
            }

            api.setBalance(balance);
            api = apiRepository.save(api);
            if (api == null){
                log.error("API Service Save Error");
                return false;
            }else
                return true;
        }catch (Exception e){
            if (e instanceof DetectedException)
                throw e;
            log.error("API Service Save Error -> " + e.getMessage());
            return false;
        }
    }



    public boolean changeState(long id, UserAction action){
        try {
            Optional<API> opt = apiRepository.findById(id);

            if (!opt.isPresent()){
                log.error("API Service Change State Error - Boyle bir api bulunamadi !");
                return false;
            }

            API api = opt.get();
            if (action == UserAction.ACTIVATE)
                api.setState(true);
            if (action == UserAction.PASSIVATE){
                if(api.isState()){
                    List<Service> services = serviceRepository.findByApi(api);
                    for (Service s: services) {

                        //packages
                        packageService.servicePassivateHandler(s);

                        //drawprizes
                        drawPrizeService.servicePassivateHandler(s);

                        s.setState(ServiceState.PASSIVE);
                    }
                    serviceRepository.saveAll(services);
                }
                api.setState(false);
            }
            apiRepository.save(api);
            return true;
        }catch (Exception e){
            log.error("User Service Change State Error - " + e.getMessage());
            return false;
        }
    }


    public String allApiUpdateBalance(){
        String message = null;
        List<API> apis = getAllAPIs();
        for (API api: apis) {
            double balance = panelService.getBalance(api);
            if (balance == -1 && api.isState()){        //api'den doğru sonuç gelmiyor kapanmış olabilir !
                message = "Durumu değişen apiler mevcut. Servislerinizi kontrol etmelisiniz.";
                log.warn(api.getId() + "Idli apiden bakiye güncelleme için doğru sonuç gelmiyor. Api artık hizmet vermiyor olabilir !");
                List<Service> services = serviceRepository.findByApi(api);
                for (Service service: services) {

                    //package
                    packageService.servicePassivateHandler(service);

                    //drawprize
                    drawPrizeService.servicePassivateHandler(service);

                    service.setState(ServiceState.PASSIVE);
                    serviceRepository.save(service);
                }
                api.setState(false);
                apiRepository.save(api);
            }else if (balance > -1 && !api.isState()){
                message = "Durumu değişen apiler mevcut. Servislerinizi kontrol etmelisiniz.";
                log.warn(api.getId() + "Idli API'den tekrar balance alınmaya başlandı !");
                api.setState(true);
                api.setBalance(balance);
                apiRepository.save(api);
            }else if (balance > -1 && api.isState()){
                api.setBalance(balance);
                apiRepository.save(api);
            }
        }
        if (message == null)
            message = "Sorunsuz bir şekilde tüm bakiyerler güncellendi !";

        return message;
    }

    @Async
    public void asyncApiUpdateBalance(API api) {
        double balance = panelService.getBalance(api);

        if (balance == -1 && api.isState()){
            log.warn(api.getId() + "Idli apiden bakiye güncelleme için doğru sonuç gelmiyor. Api artık hizmet vermiyor olabilir !");
            List<Service> services = serviceRepository.findByApi(api);
            for (Service service: services) {
                //packages
                packageService.servicePassivateHandler(service);

                //drawprizes
                drawPrizeService.servicePassivateHandler(service);

                service.setState(ServiceState.PASSIVE);
                serviceRepository.save(service);
            }
            api.setState(false);
            apiRepository.save(api);
        }else if (balance > -1 && !api.isState()){
            log.warn(api.getId() + "Idli API'den tekrar balance alınmaya başlandı !");
            api.setState(true);
            api.setBalance(balance);
            apiRepository.save(api);
        }else if (balance > -1 && api.isState()){



            if (balance < API_BALANCE_LIMIT){
                User user = userService.getAdmin();

                String mailMessage = "ID : " + api.getId() + " - Name : " + api.getName() + " -> Bakiyeniz " + API_BALANCE_LIMIT + " olarak belirlediğiniz limit değerin altına düşmüş. İlgili apiye bakiye yüklemelisiniz !";
                mailService.sendBalanceWarningMail(user.getMail(), mailMessage);
            }

            api.setBalance(balance);
            apiRepository.save(api);
        }
    }



    public String allApiUpdateActiveService(){
        try {
            List<Service> activeServices = serviceRepository.findByState(ServiceState.ACTIVE);

            List<Service> passivatedServices = new ArrayList<>();
            List<Service> deletedServices = new ArrayList<>();

            log.info("Total Active Service Count => " + activeServices.size());
            if (activeServices.size() == 0) {
                return "";
            }

            List<API> apis = apiRepository.findAll();

            for (API api: apis) {
                List<Service> apiActiveServices = new ArrayList<>();

                for (Service service: activeServices) {
                    if (service.getApi().equals(api)){
                        apiActiveServices.add(service);
                    }
                }

                log.info(api.getName() + " API Active Service Count => " + apiActiveServices.size());

                if (apiActiveServices.size()>0){
                    List<Service> clientServices = panelService.getAllServices(api);

                    if (clientServices == null){
                        return "Aktif servisleri güncelleme işi API'den servislerin çekilememesi sebebiyle başarısız oldu !";
                    }

                    log.info(api.getName() + " API Client Service Count => " + clientServices.size());

                    List<Service> targetServices = new ArrayList<>(apiActiveServices);
                    Map<Service, Service> equivalentServices = findEquivalentActiveService(clientServices, targetServices);

                    for (Service service: apiActiveServices) {
                        Service equivalentService = equivalentServices.get(service);

                        if (equivalentService == null){
                            //servis silindiğinde yapılması gereken işlemler yapılacak !
                            //bağlı paketler pasife alınmalı !
                            packageService.servicePassivateHandler(service);

                            //bağlı hediyeler pasife alınmalı !
                            drawPrizeService.servicePassivateHandler(service);

                            service.setState(ServiceState.DELETED);
                            serviceRepository.save(service);

                            deletedServices.add(service);
                        }else {
                            //değişiklik olup olmadığına bak
                            if (isChangeService(service, equivalentService)){
                                //değişiklikleri uygula
                                service = changeService(service, equivalentService);

                                //varsa bağlı paketleri ve hediyeleri pasife al
                                packageService.servicePassivateHandler(service);
                                drawPrizeService.servicePassivateHandler(service);
                                System.out.println(service.toString());
                                //pasife al ve kaydet
                                service.setState(ServiceState.PASSIVE);
                                serviceRepository.save(service);

                                //passivate listesine ekle
                                passivatedServices.add(service);
                            }
                        }
                    }
                }
            }

            String message = "";

            //mesaj oluştur !
            for (Service deletedService: deletedServices) {
                message += deletedService.getId() + " ID'li servis artık API'den gelmediği için kapatıldı! Buna bağlı paket ve çekiliş hediyeleriniz olabilir. Kontrol ediniz ! <br>";
            }

            for (Service passivatedService: passivatedServices){
                message += passivatedService.getId() + " ID'li serviste bir güncelleme tespit edildiği için kapatıldı. Buna bağlı paket ve çekiliş hediyeleriniz olabilir. Konrol ediniz ! <br>";
            }

            return message;
        }catch (Exception e){
            log.error("allApiUpdateActiveService Error -> " + e.getMessage());
            return "Aktif servisleri güncelleme işlemi başarısız oldu ! Hata mesajı : " + e.getMessage();
        }
    }

    public String allApiUpdateOtherService(){
        try {
            List<API> apis = apiRepository.findAll();

            for (API api: apis) {
                List<Service> clientServices = panelService.getAllServices(api);

                if (clientServices == null){
                    log.error("Saatte bir yapılan servis güncellemesi apiden servisler çekilemediği için başarısız oldu !");
                    return "Saatte bir yapılan servis güncellemesi apiden servisler çekilemediği için başarısız oldu !";
                }

                log.info(api.getName() + " API client service count => " + clientServices.size());
                List<Service> systemServices = serviceRepository.findByApi(api);
                log.info(api.getName() + " API system service count => " + systemServices.size());

                OtherServiceUpdateDTO res = findEquivalentOtherService(clientServices, systemServices);
                log.info("Deleted : " + res.getDeletedService().size());
                log.info("New : " + res.getNewService().size());
                log.info("Equivalents : " + res.getEquivalentMap().size());


                res.getDeletedService().forEach(service -> {
                    //aktifse zaten 5 dk'da bir kontrol ediliyor. deleted ise tekrar deleted yapmaya gerek yok
                    if (service.getState()==ServiceState.PASSIVE){
                        service.setState(ServiceState.DELETED);
                        serviceRepository.save(service);
                        log.info(service.getId() + " Servis durumu DELETED olarak değiştirildi !");
                    }
                });

                res.getNewService().forEach(service -> {
                    service = serviceRepository.save(service);
                    log.info(service.getId() + " Servis sisteme yeni eklendi !");
                });

                res.getEquivalentMap().forEach((systemService, clientService) -> {
                    if (systemService.getState()!=ServiceState.ACTIVE){    //aktifse zaten yukarıda kontrol ediliyor
                        //değişiklik olup olmadığını kontrol et - yoksa birşey yapmaya gerek yok
                        if (isChangeService(systemService, clientService)){
                            //değişiklikleri uygula
                            systemService = changeService(systemService, clientService);

                            //durumunu güncelle
                            systemService.setState(ServiceState.PASSIVE);
                            serviceRepository.save(systemService);
                            log.info(systemService.getId() + " Servis güncellendi !");
                        }
                    }
                });
            }
            return "";
        }catch (Exception e){
            log.error("allApiUpdateOtherService Error -> " + e.getMessage());
            return "Saatte bir yapılan servisleri güncelleme işlemi başarısız oldu ! Hata mesajı : " + e.getMessage();
        }
    }

    private Map<Service, Service> findEquivalentActiveService(List<Service> clientServices, List<Service> targetServices){
        Map<Service, Service> equivalentMap = new HashMap<>();
        for (Service service: clientServices) {

            for (int i = 0; i<targetServices.size(); i++) {
                if (targetServices.get(i).getApiServiceId().equals(service.getApiServiceId())){
                    equivalentMap.put(targetServices.remove(i), service);

                    if (targetServices.size() == 0)
                        return equivalentMap;
                    else
                        break;
                }
            }
        }
        log.info("Not Equivalent Service Detect !");
        for (Service service: targetServices) {
            equivalentMap.put(service, null);
        }
        return equivalentMap;
    }

    private boolean isChangeService(Service service, Service equivalentService){
        if (service.getApiMaxPiece() != equivalentService.getApiMaxPiece() ||
                service.getApiMinPiece() != equivalentService.getApiMinPiece()){
            return true;
        }

        if (service.getApi().isUseUSD() && service.getApiUSDPrice()!=equivalentService.getApiUSDPrice())
            return true;
        else if (!service.getApi().isUseUSD() && service.getApiPrice()!=equivalentService.getApiPrice())
            return true;


        return false;
    }

    private Service changeService(Service service, Service equivalentService){
        service.setApiCategory(equivalentService.getApiCategory());
        service.setApiDripfeed(equivalentService.isApiDripfeed());
        service.setApiMaxPiece(equivalentService.getApiMaxPiece());
        service.setApiMinPiece(equivalentService.getApiMinPiece());
        service.setApiName(equivalentService.getApiName());

        if(service.getApi().isUseUSD()){
            service.setApiUSDPrice(equivalentService.getApiUSDPrice());
            double apiPriceTL = service.getApiUSDPrice() * service.getApi().getRateUSD();
            service.setApiPrice(Double.valueOf(new DecimalFormat("##.##").format(apiPriceTL)));
        }else {
            service.setApiPrice(equivalentService.getApiPrice());
        }

        return service;
    }

    private OtherServiceUpdateDTO findEquivalentOtherService(List<Service> clientServices, List<Service> systemServices){
        OtherServiceUpdateDTO res = new OtherServiceUpdateDTO();
        for (Service clientService: clientServices) {
            for(int i = 0; i < systemServices.size() ; i++){
                if (clientService.getApiServiceId().equals(systemServices.get(i).getApiServiceId())){
                    res.getEquivalentMap().put(systemServices.remove(i), clientService);
                    clientService = null;
                    break;
                }
            }
            if (clientService != null){
                res.getNewService().add(clientService);
            }
        }

        for (Service deletedService: systemServices) {
            //durumu zaten deleted değilse !
            if (deletedService.getState()!=ServiceState.DELETED)
                res.getDeletedService().add(deletedService);
        }

        return res;
    }

    @Transactional
    public boolean usdRateUpdate(){
        List<API> apis = getAllAPIs();

        for (API api: apis) {
            if (api.isUseUSD()){
                double currencyUSDRate = currencyRateService.getUSD();

                if (api.getRateUSD() == currencyUSDRate){
                    throw new DetectedException("Son güncellemeden sonra dolar kurunda değişme yok !");
                }

                List<Service> services = serviceRepository.findByApi(api);

                api.setRateUSD(currencyUSDRate);
                apiRepository.save(api);

                // fiyatlar güncelleniyor
                servicesApiPriceUpdate(api);
            }
        }

        return true;
    }

    private void servicesApiPriceUpdate(API api){
        List<Service> services = serviceRepository.findByApi(api);

        services.forEach(service -> {
            double oldCustomPrice = service.getCustomPrice();

            double newApiPrice = service.getApiUSDPrice() * api.getRateUSD();

            double newCustomPrice = 0;
            if (service.getCustomPrice()!=0){
                newCustomPrice = (service.getCustomPrice()*newApiPrice)/service.getApiPrice();
                service.setCustomPrice(Double.valueOf(new DecimalFormat("##.##").format(newCustomPrice)));
            }

            service.setApiPrice(Double.valueOf(new DecimalFormat("##.##").format(newApiPrice)));
            serviceRepository.save(service);

            //bağlı paketlerin fiyatlarını değiştir
            //servis, eski custom fiyatı, yeni
            packageService.updatePackagePriceByService(service, oldCustomPrice, newCustomPrice);

            //bağlı hediyelerin fiyatları
            drawPrizeService.servicePriceUpdate(service);
            log.info(service.getId() + " Update !");
        });
    }
}
