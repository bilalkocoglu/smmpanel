package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.DrawOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrawOrderRepository extends JpaRepository<DrawOrder, Long> {
}
