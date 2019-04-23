package com.thelastcodebenders.follower.assembler;

import com.thelastcodebenders.follower.dto.CreateTicketFormDTO;
import com.thelastcodebenders.follower.model.Ticket;
import com.thelastcodebenders.follower.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class TicketAssembler {
    private static final Logger log = LoggerFactory.getLogger(TicketAssembler.class);

    public Ticket convertFormDtoToTicket(CreateTicketFormDTO ticketFormDTO, User user){
        Ticket ticket = Ticket.builder()
                .closed(false)
                .fromUser(user)
                .build();

        if (ticketFormDTO.getSubject().equals("order")){
            ticket.setSubject("Siparis - " + ticketFormDTO.getOrderNumber());
        }else if (ticketFormDTO.getSubject().equals("other")){
            ticket.setSubject(ticketFormDTO.getCustomSubject());
        }

        return ticket;
    }
}
