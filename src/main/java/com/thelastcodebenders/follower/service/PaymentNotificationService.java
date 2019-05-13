package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.assembler.PaymenNotificationAssembler;
import com.thelastcodebenders.follower.dto.PaymentNotificationFormDTO;
import com.thelastcodebenders.follower.enums.AsyncMailType;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.BankAccount;
import com.thelastcodebenders.follower.model.PaymentNotification;
import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.PaymentNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentNotificationService {
    private static final Logger log = LoggerFactory.getLogger(PaymentNotificationService.class);

    private PaymentNotificationRepository paymentNotificationRepository;
    private UserService userService;
    private PaymenNotificationAssembler paymenNotificationAssembler;
    private BankAccountService bankAccountService;
    private MailService mailService;
    private DrawService drawService;

    public PaymentNotificationService(PaymentNotificationRepository paymentNotificationRepository,
                                      UserService userService,
                                      PaymenNotificationAssembler paymenNotificationAssembler,
                                      BankAccountService bankAccountService,
                                      MailService mailService,
                                      DrawService drawService){
        this.paymentNotificationRepository = paymentNotificationRepository;
        this.userService = userService;
        this.paymenNotificationAssembler = paymenNotificationAssembler;
        this.bankAccountService = bankAccountService;
        this.mailService = mailService;
        this.drawService = drawService;
    }

    public List<String> tableColumns(){
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Id");
        columnNames.add("Kullanıcı");
        columnNames.add("Banka Adı");
        columnNames.add("Hesap Sahibi");
        columnNames.add("Tutar");
        columnNames.add("Tarih");
        columnNames.add("İşlem");
        return columnNames;
    }

    public int unconfirmedNotifications(){
        return (int)paymentNotificationRepository.countByConfirmation(false);
    }

    public int unconfirmedLoginUserNotifications() throws LoginException {
        List<PaymentNotification> paymentNotifications = getLoginUserPaymentNotifications();

        int count = 0;

        for (PaymentNotification paymentNotification: paymentNotifications) {
            if (!paymentNotification.isConfirmation())
                count++;
        }
        return count;
    }

    public List<PaymentNotification> allPaymentNotifications(){
        return paymentNotificationRepository.findAll();
    }

    public List<PaymentNotification> getLoginUserPaymentNotifications() throws LoginException {
        User loginUser = userService.getAuthUser();

        if (loginUser != null){
            return paymentNotificationRepository.findByUser(loginUser);
        }else {
            log.error("Payment Notification Service get Login User Notifications Error ! -> login user = null");
            return null;
        }
    }

    @Transactional
    public boolean confirmPayment(long paymentNotificationId){
        try {
            Optional<PaymentNotification> optPaymentNot = paymentNotificationRepository.findById(paymentNotificationId);

            if (optPaymentNot.isPresent()){
                PaymentNotification paymentNotification = optPaymentNot.get();
                if (paymentNotification.isConfirmation()){
                    log.error("Payment Notification Error ! - Bu bildirim zaten onaylanmis !");
                    return false;
                }
                paymentNotification.setConfirmation(true);
                boolean res = userService.updateUserBalance(paymentNotification.getUser(), paymentNotification.getAmount());
                if (!res)
                    return false;
                drawService.addDrawCount(paymentNotification.getUser());
                paymentNotification = paymentNotificationRepository.save(paymentNotification);
                mailService.asyncSendMail(AsyncMailType.PAYMENTNTFRES, paymentNotification.getUser(), paymentNotification.getUser(), "");
                return true;
            }else {
                log.error("Payment Notification Error ! - Boyle bir odeme bildirimi bulunamadi !");
                return false;
            }
        }catch (Exception e){
            log.error("Payment Notification Error ! - " + e.getMessage());
            return false;
        }
    }

    public boolean createPaymentNotification(PaymentNotificationFormDTO paymentNtfForm) throws LoginException {
        if (!validatePaymentNtfForm(paymentNtfForm)){
            throw new DetectedException("Tüm alanları eksiksiz doldurmalısınız !");
        }

        PaymentNotification paymentNotification = paymenNotificationAssembler.convertFormDtoToPaymentNtf(paymentNtfForm);
        paymentNotification.setUser(userService.getAuthUser());

        BankAccount bankAccount = bankAccountService.findById(paymentNtfForm.getBankAccountId());
        if (bankAccount != null){
            paymentNotification.setBankAccount(bankAccount);
        }else
            return false;

        paymentNotification = paymentNotificationRepository.save(paymentNotification);
        if (paymentNotification != null){
            mailService.asyncSendMail(AsyncMailType.PAYMENTNTFREQ, paymentNotification.getUser(), userService.getAdmin(), "");
            return true;
        }
        else{
            log.error("Payment Notification Save Error !");
            return false;
        }
    }

    private boolean validatePaymentNtfForm(PaymentNotificationFormDTO paymentNotificationForm){
        if (isNullOrEmpty(paymentNotificationForm.getFullName()) || isNullOrEmpty(paymentNotificationForm.getDate()) || isNullOrEmpty(paymentNotificationForm.getTime())){
            throw new DetectedException("Tüm alanları eksiksiz doldurmalısınız !");
        }else if (paymentNotificationForm.getFullName().length()>80 || paymentNotificationForm.getDate().length()>80 || paymentNotificationForm.getTime().length()>80){
            throw new DetectedException("Tüm alanları doğru girmelisiniz !");
        }
        return true;
    }

    private static boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }

}
