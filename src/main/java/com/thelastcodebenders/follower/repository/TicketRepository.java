package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.Ticket;
import com.thelastcodebenders.follower.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByClosed(boolean closed);

    List<Ticket> findByFromUserAndClosed(User fromUser, boolean closed);

    List<Ticket> findByFromUser(User fromUser);
}
