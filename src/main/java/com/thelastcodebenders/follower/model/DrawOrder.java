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
@Table(name = "draworder")
public class DrawOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "draworder_id")
    private long id;

    @ManyToOne
    @JoinColumn
    private DrawPrize drawPrize;

    @ManyToOne
    @JoinColumn
    private User user;

    private LocalDateTime dateTime;
}
