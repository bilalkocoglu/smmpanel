package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.model.CardPayment;
import com.thelastcodebenders.follower.repository.CardPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
        List<String> columns = Stream.of("Id", "Kullanıcı", "Tutar", "Tarih").collect(Collectors.toList());
        return columns;
    }

    public List<CardPayment> allCardPayment(){
        return cardPaymentRepository.findAll();
    }
}
