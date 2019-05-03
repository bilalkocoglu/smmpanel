package com.thelastcodebenders.follower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "visitor_message")
public class VisitorMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "visitor_message_id")
    private long id;
    private String ipAddr;
    private LocalDateTime localDateTime;
}
