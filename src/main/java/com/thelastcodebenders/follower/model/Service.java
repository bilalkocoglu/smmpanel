package com.thelastcodebenders.follower.model;

import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.enums.UserAction;
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
@Table(name = "service")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "service_id")
    private Long id;
    private String apiServiceId;       //apiden gelen servis idsi


    private ServiceState state;

    private int customMaxPiece;
    private int customMinPiece;
    private int apiMaxPiece;           //max sipariş miktarı
    private int apiMinPiece;           //min sipariş miktarı

    private String customName;
    private String apiName;

    private boolean apiDripfeed;

    private double apiPrice;            //alis fiyati
    private double customPrice;     //satis fiyati

    @Column(length = 3000)
    private String description;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "fk_api")
    private API api;

    @ManyToOne
    @JoinColumn(name = "fk_subcategory")
    private SubCategory subCategory;
}
