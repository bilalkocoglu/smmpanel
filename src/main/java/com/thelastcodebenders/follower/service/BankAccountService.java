package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.model.BankAccount;
import com.thelastcodebenders.follower.repository.BankAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BankAccountService {
    private static final Logger log = LoggerFactory.getLogger(BankAccountService.class);

    private BankAccountRepository bankAccountRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository){
        this.bankAccountRepository = bankAccountRepository;
    }

    public void save(BankAccount bankAccount){
        try {
            bankAccountRepository.save(bankAccount);
        }catch (Exception e){
            log.error("Bank Account - save - Error ! -> " + e.getMessage());
            throw e;
        }
    }

    public List<BankAccount> allAccounts(){
        return bankAccountRepository.findAll();
    }

    public boolean deleteAccount(long id){
        try {
            bankAccountRepository.deleteById(id);
            return true;
        }catch (Exception e){
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
