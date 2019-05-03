package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.VisitorMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitorMessageRepository extends JpaRepository<VisitorMessage, Long> {
    List<VisitorMessage> findByIpAddr(String ipAddr);
}
