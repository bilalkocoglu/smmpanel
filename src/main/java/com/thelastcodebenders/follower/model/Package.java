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
@Table(name = "package")
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "package_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_service")
    private Service service;

    private String name;
    private int quantity;
    private double price;
    @Column(length = 3000)
    private String description;
    private boolean state;
}
