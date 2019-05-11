package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.model.CardPayment;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.CardPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public List<CardPayment> allCardPayment(){
        return cardPaymentRepository.findAll();
    }

    public void newSuccessCardPayment(User user, double amount, String iyzipayToken){
        try {
            CardPayment cardPayment = new CardPayment();
            cardPayment.setUser(user);
            cardPayment.setAmount(amount);
            cardPayment.setIyzipayToken(iyzipayToken);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            cardPayment.setDate(LocalDateTime.now().format(formatter));
            cardPaymentRepository.save(cardPayment);
        }catch (Exception e){
            log.error("New Success Card Payment Save Error -> " + e.getMessage());
        }
    }
}
