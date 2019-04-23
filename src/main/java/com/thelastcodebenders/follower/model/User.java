package com.thelastcodebenders.follower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Positive;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private Long id;
    private String name;
    private String surname;
    private String mail;
    private String password;
    private boolean state;

    @ManyToOne
    @JoinColumn(name = "fk_role")
    private Role role;

    private String number;
    private double balance;

    // kayıt tarihi eklenecek telefon numarası opsiyonel olacak !
}
