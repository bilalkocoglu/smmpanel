package com.thelastcodebenders.follower.assembler;

import com.thelastcodebenders.follower.dto.PaymentNotificationFormDTO;
import com.thelastcodebenders.follower.model.BankAccount;
import com.thelastcodebenders.follower.model.PaymentNotification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class PaymenNotificationAssembler {
    public PaymentNotification convertFormDtoToPaymentNtf(PaymentNotificationFormDTO paymentNtfForm){
        return PaymentNotification.builder()
                .time(convertTime(paymentNtfForm.getTime()))
                .fullname(paymentNtfForm.getFullName())
                .date(convertDate(paymentNtfForm.getDate()))
                .confirmation(false)
                .amount(paymentNtfForm.getAmount())
                .build();
    }

    private LocalDate convertDate(String dateString){
        String[] dataParses = dateString.split(" ");
        if (dataParses[0].length() == 1){
            dataParses[0] = '0'+dataParses[0];
        }
        if (dataParses[1].length() > 4){
            dataParses[1] = dataParses[1].substring(0,3) + ",";
        }
        dateString = dataParses[0] + ' ' + dataParses[1] + ' ' + dataParses[2];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy");
        LocalDate localDate = LocalDate.parse(dateString, formatter);
        return localDate;
    }

    private LocalTime convertTime(String timeString){
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime localTime = LocalTime.parse(timeString, timeFormatter);
        return localTime;
    }
}
