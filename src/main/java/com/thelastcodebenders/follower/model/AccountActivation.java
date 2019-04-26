package com.thelastcodebenders.follower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "accountactivation")
public class AccountActivation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "accountactivationid")
    private long id;

    @ManyToOne
    @JoinColumn(name = "fk_accountactivation_user")
    private User user;
    private String secretkey;
}
