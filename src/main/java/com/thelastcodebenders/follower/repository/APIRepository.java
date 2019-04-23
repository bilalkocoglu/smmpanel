package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.API;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APIRepository extends JpaRepository<API, Long> {
    List<API> findByUrl(String url);
}
