package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.Order;
import com.thelastcodebenders.follower.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user, Sort sort);

    List<Order> findByClosed(boolean closed);

    int countByUserAndClosed(User user, boolean closed);
}
