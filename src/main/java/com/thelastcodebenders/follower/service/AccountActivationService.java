package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.enums.AsyncMailType;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.AccountActivation;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.AccountActivationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AccountActivationService {
    private static final Logger log = LoggerFactory.getLogger(AccountActivationService.class);
    private static final int KEY_COUNT = 25;
    private static final String[] LATTERS = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
            "p","r","s","t","u","v","y","z","0","1","2","3","4","5","6","7","8","9"};
    private static Random rnd = new Random();

    private AccountActivationRepository accountActivationRepository;
    private MailService mailService;

    public AccountActivationService(AccountActivationRepository accountActivationRepository,
                                    MailService mailService){
        this.accountActivationRepository = accountActivationRepository;
        this.mailService = mailService;
    }

    public void generateSecretKey(User user){
        String secretKey = generateKey();

        boolean alreadyKey = true;
        while (alreadyKey){
            if (accountActivationRepository.countBySecretkey(secretKey)>0){
                secretKey = generateKey();
            }else {
                alreadyKey = false;
            }
        }

        AccountActivation accountActivation = AccountActivation.builder().secretkey(secretKey).user(user).build();
        accountActivation = accountActivationRepository.save(accountActivation);

        if (accountActivation != null){
            mailService.asyncSendMail(AsyncMailType.ACCOUNTACTIVATE, user, user, accountActivation.getSecretkey());
        }
    }

    public void sendKeyAgain(User user){
        String key = getKeyByUser(user);

        mailService.asyncSendMail(AsyncMailType.ACCOUNTACTIVATE, user, user, key);
    }

    private String generateKey(){
        String key = "";

        for (int i=0 ; i<KEY_COUNT ; i++){
            int index = rnd.nextInt(LATTERS.length);
            key+=LATTERS[index];
        }

        return key;
    }

    public User getUserByKey(String key){
        List<AccountActivation> accountActivations = accountActivationRepository.findBySecretkey(key);

        if (accountActivations.isEmpty()){
            log.error("Account Activate Service Get User By Key Error -> Key Not Found !");
            return null;
        }else {
            return accountActivations.get(0).getUser();
        }
    }

    public String getKeyByUser(User user){
        List<AccountActivation> accountActivations = accountActivationRepository.findByUser(user);

        if (accountActivations.isEmpty()){
            log.error("Account Activation Service getKeyByUser Error -> " +user.getId() + " kullanıcısına tanımlanmış anahtar yok !");
            throw new DetectedException("Bu kullanıcıya ait anahtar yok !");
        }else {
            return accountActivations.get(0).getSecretkey();
        }
    }

    public String generateRandomPassword(int lenght){
        String rndPass = "";
        for (int i=0; i<lenght ; i++){
            int index = rnd.nextInt(LATTERS.length);
            rndPass+=LATTERS[index];
        }
        return rndPass;
    }
}
