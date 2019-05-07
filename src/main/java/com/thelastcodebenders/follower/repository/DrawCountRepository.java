package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.DrawCount;
import com.thelastcodebenders.follower.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrawCountRepository extends JpaRepository<DrawCount, Long> {
    List<DrawCount> findByUser(User user);
}
