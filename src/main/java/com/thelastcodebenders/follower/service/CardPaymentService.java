package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.CardPayment;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.CardPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CardPaymentService {
    private static final Logger log = LoggerFactory.getLogger(CardPaymentService.class);

    private CardPaymentRepository cardPaymentRepository;

    public CardPaymentService(CardPaymentRepository cardPaymentRepository){
        this.cardPaymentRepository = cardPaymentRepository;
    }

    public List<String> tableColumns(){
        List<String> columns = Stream.of("Id", "Kullanıcı", "Tutar", "Tarih", "Token").collect(Collectors.toList());
        return columns;
    }

    public List<CardPayment> findFinishedCardPayment(){
        return cardPaymentRepository.findByFinished(true);
    }

    public boolean createCardPayment(User user, String token, int balance){
        try {
            CardPayment cardPayment = new CardPayment();
            cardPayment.setId(token);
            cardPayment.setUser(user);
            cardPayment.setAmount(balance);
            cardPayment.setFinished(false);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            cardPayment.setDate(LocalDateTime.now().format(formatter));

            cardPaymentRepository.save(cardPayment);
            return true;
        }catch (Exception e){
            log.error("Card Payment Service createCardPayment Error => " + e.getMessage());
            return false;
        }
    }

    public CardPayment findActiveByToken(String merchant_oid){
        List<CardPayment> cardPayments = cardPaymentRepository.findByIdAndFinished(merchant_oid, false);

        if (cardPayments.isEmpty()){
            throw new DetectedException("Böyle bir CardPayment Bulunamadı !");
        }else {
            return cardPayments.get(cardPayments.size()-1);
        }
    }

    public CardPayment update(CardPayment cardPayment){
        return cardPaymentRepository.save(cardPayment);
    }
}
