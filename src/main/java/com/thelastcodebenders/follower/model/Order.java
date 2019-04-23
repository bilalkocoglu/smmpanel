package com.thelastcodebenders.follower.model;

import com.thelastcodebenders.follower.enums.OrderStatusType;
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
@Table(name = "systemorder")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User user;                                          //+

    @ManyToOne
    private Package packagee;

    @ManyToOne
    private Service service;                                    //+

    private int quantity;                                       //+
    private double apiPrice;                                    //+
    private double customPrice;                                 //+

    private String destUrl;                                     //+

    private OrderStatusType status; //complated , Canceled      +
    private String apiOrderId;
    private String date;                                        //+

    private int remain;                                      //+
    private double remainBalance;                               //+
    private int startCount;

    private boolean closed;                                   //+
}
