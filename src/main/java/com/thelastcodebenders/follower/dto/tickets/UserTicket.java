package com.thelastcodebenders.follower.dto.tickets;

import com.thelastcodebenders.follower.model.Message;
import com.thelastcodebenders.follower.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTicket {
    private Ticket ticket;
    private String lastChange;
    private boolean answer;
    private List<Message> messages;
}
