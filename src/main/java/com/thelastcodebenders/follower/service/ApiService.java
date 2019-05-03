package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.client.ClientService;
import com.thelastcodebenders.follower.enums.MailType;
import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.enums.UserAction;
import com.thelastcodebenders.follower.model.API;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.APIRepository;
import com.thelastcodebenders.follower.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.thelastcodebenders.follower.model.Service;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

@org.springframework.stereotype.Service
public class ApiService {
    private static final Logger log  = LoggerFactory.getLogger(ApiService.class);

    private static final double API_BALANCE_LIMIT = 3;

    private APIRepository apiRepository;
    private ClientService clientService;
    private ServiceRepository serviceRepository;
    private PackageService packageService;
    private MailService mailService;
    private UserService userService;

    public ApiService(APIRepository apiRepository,
                      ClientService clientService,
                      ServiceRepository serviceRepository,
                      PackageService packageService,
                      MailService mailService,
                      UserService userService){
        this.apiRepository = apiRepository;
        this.clientService = clientService;
        this.serviceRepository = serviceRepository;
        this.packageService = packageService;
        this.mailService = mailService;
        this.userService = userService;
    }

    public List<String> adminTableColumns(){
        List<String> columns = new ArrayList<>();
        columns.add("Name");
        columns.add("Url");
        columns.add("State");
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
                throw new RuntimeException("Eklemek istediğiniz Api ile aynı url e sahip başka bir Api mevcut !");

            api.setState(true);
            double balance = clientService.getBalance(api);
            if (balance == -1){
                throw new RuntimeException("Eklemek istediğiniz api kullanıma uygun değil !");
            }
            List<Service> services = clientService.getAllServices(api);
            if (services == null && services.isEmpty()){
                throw new RuntimeException("Eklemek istediğiniz api kullanıma uygun değil !");
            }

            for (Service service: services) {
                serviceRepository.save(service);
            }

            api.setBalance(balance);
            api = apiRepository.save(api);
            if (api == null){
                log.error("API Service Save Error");
                return false;
            }else
                return true;
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("API Service Save Error -> " + e.getMessage());
            return false;
        }
    }

    public boolean deleteApi(long apiId){
        try {
            API api = apiRepository.findById(apiId).get();
            List<Service> services = serviceRepository.findByApi(api);
            if (services.isEmpty()){
                apiRepository.deleteById(apiId);
                return true;
            }else {
                log.error("API Service API Delete Error -> Bu apiye bagli servisler mevcut!");
                throw new RuntimeException("Bu API'ye bağlı servisler mevcut, bu yüzden silemezsiniz !");
            }
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("API Service API Delete Error -> " + e.getMessage());
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
                        List<Package> packages = packageService.findPackageByService(s);
                        for (Package pkg: packages) {
                            pkg.setState(false);
                        }
                        packageService.saveAll(packages);
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
            double balance = clientService.getBalance(api);
            if (balance == -1 && api.isState()){        //api'den doğru sonuç gelmiyor kapanmış olabilir !
                message = "Durumu değişen apiler mevcut. Servislerinizi kontrol etmelisiniz.";
                log.warn(api.getId() + "Idli apiden bakiye güncelleme için doğru sonuç gelmiyor. Api artık hizmet vermiyor olabilir !");
                List<Service> services = serviceRepository.findByApi(api);
                for (Service service: services) {

                    List<Package> packages = packageService.findPackageByService(service);
                    for (Package pkg: packages) {
                        pkg.setState(false);
                    }
                    packageService.saveAll(packages);

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
        double balance = clientService.getBalance(api);

        if (balance == -1 && api.isState()){
            log.warn(api.getId() + "Idli apiden bakiye güncelleme için doğru sonuç gelmiyor. Api artık hizmet vermiyor olabilir !");
            List<Service> services = serviceRepository.findByApi(api);
            for (Service service: services) {
                List<Package> packages = packageService.findPackageByService(service);
                for (Package pkg: packages) {
                    pkg.setState(false);
                }
                packageService.saveAll(packages);

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

    public String allApiUpdateService(){
        try {
            log.info("All Service Update !!!");
            String message = "";
            List<Long> updatedServices = new ArrayList<Long>();
            List<Long> newServices = new ArrayList<Long>();
            List<Long> deletedServices = new ArrayList<Long>();

            List<API> apis = apiRepository.findAll();

            for (API api: apis) {
                List<Service> apiServices = clientService.getAllServices(api);

                if (apiServices == null){
                    throw new RuntimeException("Api'den Servisler çekilemedi !");
                }

                Stack<Service> stackApiServices = new Stack<>();
                stackApiServices.addAll(apiServices);

                List<Service> customServices = serviceRepository.findByApi(api);

                while (!stackApiServices.empty()){
                    Service apiService = stackApiServices.pop();

                    Service equivalentService = null;     //eşleşen service
                    for (int i = 0; i<customServices.size(); i++) {
                        if (customServices.get(i).getApiServiceId().equals(apiService.getApiServiceId())){
                            equivalentService = customServices.get(i);
                            customServices.remove(i);
                            break;
                        }
                    }

                    if (equivalentService == null){
                        //eşleşen servis yok bu servis yeni
                        apiService = serviceRepository.save(apiService);
                        log.info("Api Service Update Service Method -> " + apiService.getId() + "-Idli servis sisteme yeni eklendi !");
                        newServices.add(apiService.getId());
                    }else {
                        //eşleşen servis var değişiklik olup olmadığı kontrol edilecek.
                        if (equivalentService.getApiMaxPiece() != apiService.getApiMaxPiece() ||
                                equivalentService.getApiMinPiece() != apiService.getApiMinPiece() ||
                                equivalentService.getApiPrice() != apiService.getApiPrice()){
                            //değişiklik var
                            equivalentService.setApiMaxPiece(apiService.getApiMaxPiece());
                            equivalentService.setApiMinPiece(apiService.getApiMinPiece());
                            equivalentService.setApiPrice(apiService.getApiPrice());
                            equivalentService.setApiName(apiService.getApiName());
                            equivalentService.setState(ServiceState.PASSIVE);

                            List<Package> packages = packageService.findPackageByService(equivalentService);
                            for (Package pkg: packages) {
                                pkg.setState(false);
                            }
                            packageService.saveAll(packages);

                            log.info("Api Service Update Service Method -> " + equivalentService.getId() + "-Idli serviste apiden gelen değişiklikler mevcut. Servislerinizi ve paketlerinizi kontrol ediniz !");
                            updatedServices.add(equivalentService.getId());
                            serviceRepository.save(equivalentService);
                        }else if (equivalentService.getState() == ServiceState.DELETED){
                            equivalentService.setState(ServiceState.PASSIVE);
                            log.info("Api Service Update Service Method -> " + equivalentService.getId() + "-Idli serviste apiden gelen değişiklikler mevcut. Servislerinizi ve paketlerinizi kontrol ediniz !");
                            serviceRepository.save(equivalentService);
                            updatedServices.add(equivalentService.getId());
                        }
                    }
                }
                if (customServices.size() > 0){
                    //api tarafından durdurulan servisler var demektir.
                    for (Service service : customServices){
                        if(service.getState() != ServiceState.DELETED){
                            service.setState(ServiceState.DELETED);

                            List<Package> packages = packageService.findPackageByService(service);
                            for (Package pkg: packages) {
                                pkg.setState(false);
                            }
                            packageService.saveAll(packages);
                            log.info("Api Service Update Service Method -> " + service.getId() + "Idli servis artık apiden gelmiyor. Servislerinizi ve paketlerinizi kontrol ediniz !");
                            serviceRepository.save(service);
                            deletedServices.add(service.getId());
                        }
                    }
                }

                if (newServices.size()>0){
                    for (long serviceId : newServices){
                        message += serviceId + " ID'li servis sisteme yeni eklendi !<br>";
                    }
                }
                if (updatedServices.size()>0){
                    for (long serviceId : updatedServices){
                        message += serviceId + " ID'li servis güncellendi ve pasif duruma getirildi ! Servis ve paketlerinizi kontrol ediniz !<br>";
                    }
                }
                if (deletedServices.size()>0){
                    for (long serviceId : deletedServices){
                        message += serviceId + " ID'li servis artık apide aktif değil ! Servis ve paketlerinizi kontrol ediniz !<br>";
                    }
                }
            }
            return message;

        }catch (Exception e){
            log.error("Api Service All Service Update Error ! -> " + e.getMessage());
            return "Servisleri güncelleme esnasında bir hata ile karşılaşıldı. Lütfen daha sonra tekrar deneyin !";
        }
    }
}
