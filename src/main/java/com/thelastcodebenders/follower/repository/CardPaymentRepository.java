package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.CardPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardPaymentRepository extends JpaRepository<CardPayment, Long> {
    List<CardPayment> findByFinished(boolean finished);

    List<CardPayment> findByTokenAndFinished(String token, boolean finished);
}
