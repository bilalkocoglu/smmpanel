package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.DrawVisit;
import com.thelastcodebenders.follower.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrawVisitRepository extends JpaRepository<DrawVisit, Long> {
    List<DrawVisit> findByUser(User user, Sort sort);
}
