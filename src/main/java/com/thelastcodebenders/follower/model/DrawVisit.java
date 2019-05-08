package com.thelastcodebenders.follower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "drawvisit")
public class DrawVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "drawvisit_id")
    private long id;

    @ManyToOne
    @JoinColumn
    private User user;

    @ManyToOne
    @JoinColumn
    private DrawOrder drawOrder;

    private LocalDateTime date;
}
