package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.configuration.cache.CacheService;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.BankAccount;
import com.thelastcodebenders.follower.repository.BankAccountRepository;
import com.thelastcodebenders.follower.repository.PaymentNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BankAccountService {
    private static final Logger log = LoggerFactory.getLogger(BankAccountService.class);

    private BankAccountRepository bankAccountRepository;
    private PaymentNotificationRepository paymentNotificationRepository;
    private CacheService cacheService;

    public BankAccountService(BankAccountRepository bankAccountRepository,
                              PaymentNotificationRepository paymentNotificationRepository,
                              CacheService cacheService){
        this.bankAccountRepository = bankAccountRepository;
        this.paymentNotificationRepository = paymentNotificationRepository;
        this.cacheService = cacheService;
    }

    public void save(BankAccount bankAccount){
        try {
            bankAccountRepository.save(bankAccount);
            cacheService.bankaccountsClear();
            allAccounts();
        }catch (Exception e){
            log.error("Bank Account - save - Error ! -> " + e.getMessage());
            throw e;
        }
    }

    @Cacheable("bankAccounts")
    public List<BankAccount> allAccounts(){
        return bankAccountRepository.findAll();
    }

    public boolean deleteAccount(long id){
        try {
            BankAccount bankAccount = findById(id);

            if (bankAccount == null)
                return false;

            if (paymentNotificationRepository.countByBankAccount(bankAccount)>0){
                throw new DetectedException("Bu banka hesabına bağlı olarak oluşturulmuş ödeme bildirimleri mevcut, bu yüzden silinemez !");
            }

            bankAccountRepository.deleteById(id);
            cacheService.bankaccountsClear();
            allAccounts();
            return true;
        }catch (Exception e){
            if (e instanceof DetectedException)
                throw e;
            log.error("Delete account exception ! - " + e.getMessage());
            return false;
        }
    }

    public BankAccount findById(long accountId){
        try {
            Optional<BankAccount> opt = bankAccountRepository.findById(accountId);

            if (opt.isPresent())
                return opt.get();
            else {
                log.error("Bank Account Service Find By Id Error -> Not Found");
                return null;
            }
        }catch (Exception e){
            log.error("Bank Account Service Find By Id Error -> " + e.getMessage());
            return null;
        }
    }
}
