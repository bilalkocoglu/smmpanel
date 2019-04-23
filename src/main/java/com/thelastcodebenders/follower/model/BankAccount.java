package com.thelastcodebenders.follower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bank_account")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "bank_account_id")
    private Long id;

    @NotNull
    @NotEmpty
    private String bankName;

    @NotNull
    @NotEmpty
    private String IBAN;

    @NotNull
    @NotEmpty
    private String accountHolder; //hesap sahibi

    @NotNull
    @NotEmpty
    private String accountNumber;

    @NotNull
    @NotEmpty
    private String branch;      //şube
}
