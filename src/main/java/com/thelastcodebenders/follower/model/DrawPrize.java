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
@Table(name = "drawprize")
public class DrawPrize {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "drawprize_id")
    private long id;

    private String name;
    private int quantity;
    private double apiPrice;

    @ManyToOne
    @JoinColumn
    private Service service;

    private boolean state;
}
