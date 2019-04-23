package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.Message;
import com.thelastcodebenders.follower.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByTicket(Ticket ticket);
}
