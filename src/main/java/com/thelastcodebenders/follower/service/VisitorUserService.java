package com.thelastcodebenders.follower.service;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.thelastcodebenders.follower.dto.PackageOrderPaymentFormDTO;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.VisitorUser;
import com.thelastcodebenders.follower.repository.VisitorUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VisitorUserService {
    private static final Logger log = LoggerFactory.getLogger(VisitorUserService.class);

    private VisitorUserRepository visitorUserRepository;
    private OrderService orderService;

    public VisitorUserService(VisitorUserRepository visitorUserRepository,
                              OrderService orderService){
        this.visitorUserRepository = visitorUserRepository;
        this.orderService = orderService;
    }

    public VisitorUser save(PackageOrderPaymentFormDTO packageOrderPaymentForm, long packageId){
        try {
            packageOrderFormValidate(packageOrderPaymentForm);

            List<VisitorUser> visitorUsers = visitorUserRepository.findByEmail(packageOrderPaymentForm.getEmail());

            VisitorUser visitorUser;

            if (visitorUsers.isEmpty()){
                visitorUser = VisitorUser.builder()
                        .email(packageOrderPaymentForm.getEmail())
                        .name(packageOrderPaymentForm.getName())
                        .number(packageOrderPaymentForm.getNumber())
                        .surname(packageOrderPaymentForm.getSurname())
                        .url(packageOrderPaymentForm.getUrl())
                        .packageId(packageId)
                        .build();

                visitorUser = visitorUserRepository.save(visitorUser);
                return visitorUser;
            }else {
                visitorUser = visitorUsers.get(0);
                visitorUser.setName(packageOrderPaymentForm.getName());
                visitorUser.setNumber(packageOrderPaymentForm.getNumber());
                visitorUser.setSurname(packageOrderPaymentForm.getSurname());
                visitorUser.setUrl(packageOrderPaymentForm.getUrl());
                visitorUser.setPackageId(packageId);

                visitorUser = visitorUserRepository.save(visitorUser);
                return visitorUser;
            }
        }catch (Exception e){
            if (e instanceof DetectedException)
                throw e;

            log.error("Visitor User Service Save Error => " + e.getMessage());
            throw new DetectedException("İşleminiz şu anda yapılamadı. Lütfen daha sonra tekrar deneyin.");
        }
    }

    private boolean packageOrderFormValidate(PackageOrderPaymentFormDTO packageOrderPaymentForm){
        if (isNullOrEmpty(packageOrderPaymentForm.getEmail()) || isNullOrEmpty(packageOrderPaymentForm.getName())
                || isNullOrEmpty(packageOrderPaymentForm.getNumber()) || isNullOrEmpty(packageOrderPaymentForm.getSurname())
                || isNullOrEmpty(packageOrderPaymentForm.getUrl())){
            throw new DetectedException("Tüm alanları eksiksiz doldurmalısınız !");
        }
        else if (packageOrderPaymentForm.getEmail().length()>60 || packageOrderPaymentForm.getName().length()>30
                || packageOrderPaymentForm.getNumber().length() >20  || packageOrderPaymentForm.getSurname().length() > 30
                || packageOrderPaymentForm.getUrl().length()>40){
            throw new DetectedException("Tüm alanları doğru doldurmalısınız !");
        }
        else if (!packageOrderPaymentForm.isCheckbox()){
            throw new DetectedException("Sipariş verebilmek için kullanım koşullarını kabul etmelisiniz !");
        }
        else if (orderService.isAlreadyUrl(packageOrderPaymentForm.getUrl())){
            throw new DetectedException("Girdiğiniz url için sistemimizde tamamlanmamış sipariş mevcut. Lütfen daha sonra tekrar deneyiniz.");
        }
        else
            return true;
    }

    private static boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }


    public VisitorUser findById(long id){
        Optional<VisitorUser> opt = visitorUserRepository.findById(id);

        if (opt.isPresent())
            return opt.get();
        else {
            log.error("VisitorUserService findById Error -> " + id + " Not Found !");
            return null;
        }
    }

    public VisitorUser findByToken(String token){
        List<VisitorUser> visitorUsers = visitorUserRepository.findByToken(token);

        if (visitorUsers.isEmpty()){
            throw new DetectedException("Ödeme alındı fakat sipariş verilemedi. Konu hakkında sayfa altındaki yer alan iletişim formundan bizimle iletişime geçiniz.");
        }else
            return visitorUsers.get(0);
    }

    public void resetToken(long id){
        try {
            System.out.println("token reset run !");
            VisitorUser visitorUser = findById(id);
            visitorUser.setToken("");

            visitorUser = visitorUserRepository.save(visitorUser);
            System.out.println(visitorUser);
        }catch (Exception e){
            log.error("VisitorUserService resetToken Error -> " + e.getMessage());
        }
    }

    public void updateToken(long id, String token){
        System.out.println("token update run");
        VisitorUser visitorUser = findById(id);

        if (visitorUser == null){
            throw new DetectedException("İşleminiz şu anda yapılamadı. Lütfen daha sonra tekrar deneyin.");
        }

        visitorUser.setToken(token);
        visitorUser = visitorUserRepository.save(visitorUser);
        System.out.println(visitorUser);
    }

}
