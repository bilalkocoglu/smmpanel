package com.thelastcodebenders.follower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "visitor_user")
public class VisitorUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "visitor_user_id")
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String number;
    private String url;
    private String token;

    @ManyToOne
    @JoinColumn
    private Package pkg;

    private boolean finished;
}
