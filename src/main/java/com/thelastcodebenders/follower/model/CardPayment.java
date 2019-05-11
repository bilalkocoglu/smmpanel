package com.thelastcodebenders.follower.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "card_payment")
public class CardPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "card_payment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_cardpayment_user")
    private User user;

    private double amount;

    private String date;

    private String iyzipayToken;
}
