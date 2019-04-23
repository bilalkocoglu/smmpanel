package com.thelastcodebenders.follower.assembler;

import com.thelastcodebenders.follower.model.Message;
import com.thelastcodebenders.follower.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageAssembler {
    private static final Logger log = LoggerFactory.getLogger(MessageAssembler.class);

    public Message convertStringToMessage(String message, boolean isUser, Ticket ticket){
        return Message.builder()
                .ticket(ticket)
                .isUser(isUser)
                .message(message)
                .date(LocalDateTime.now())
                .build();
    }
}
