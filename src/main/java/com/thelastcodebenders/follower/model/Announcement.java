package com.thelastcodebenders.follower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "announcement")
public class Announcement {
    //duyurular - ck editor
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "announcement_id")
    private Long id;
    private LocalDate date;

    @NotNull
    @NotEmpty
    private String title;

    @NotNull
    @NotEmpty
    @Column(length = 1000)
    private String description;
}
