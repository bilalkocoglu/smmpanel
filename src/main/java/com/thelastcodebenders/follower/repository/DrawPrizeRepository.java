package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.DrawPrize;
import com.thelastcodebenders.follower.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrawPrizeRepository extends JpaRepository<DrawPrize, Long> {
    List<DrawPrize> findByService(Service service);
}
