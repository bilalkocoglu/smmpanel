package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.VisitorUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitorUserRepository extends JpaRepository<VisitorUser, Long> {
    List<VisitorUser> findByEmail(String email);

    List<VisitorUser> findByToken(String token);
}
