package com.thelastcodebenders.follower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "drawcount")
public class DrawCount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "drawcount_id")
    private long id;

    @ManyToOne
    @JoinColumn
    private User user;

    private int count;
}
