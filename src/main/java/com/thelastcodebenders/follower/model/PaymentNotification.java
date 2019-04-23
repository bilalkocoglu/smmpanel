package com.thelastcodebenders.follower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "paymentNotification")
public class PaymentNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_notification_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_user")
    private User user;

    @ManyToOne
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;
    private String fullname;    //hesap sahibinin
    private double amount;
    private LocalDate date;
    private LocalTime time;
    private boolean confirmation;   //true:onaylanmış, false:onaylanmamış
}
