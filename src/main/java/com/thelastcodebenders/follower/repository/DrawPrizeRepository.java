package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.DrawPrize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrawPrizeRepository extends JpaRepository<DrawPrize, Long> {
}
