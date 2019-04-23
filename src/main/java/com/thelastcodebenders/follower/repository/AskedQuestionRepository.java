package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.AskedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AskedQuestionRepository extends JpaRepository<AskedQuestion, Long> {
}
