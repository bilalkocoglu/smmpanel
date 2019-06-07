package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.BankAccount;
import com.thelastcodebenders.follower.model.PaymentNotification;
import com.thelastcodebenders.follower.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PaymentNotificationRepository extends JpaRepository<PaymentNotification, Long> {
    long countByConfirmation(boolean state);

    List<PaymentNotification> findByUser(User user);

    long countByBankAccount(BankAccount bankAccount);

    List<PaymentNotification> findByConfirmation(boolean confirmation);
}
