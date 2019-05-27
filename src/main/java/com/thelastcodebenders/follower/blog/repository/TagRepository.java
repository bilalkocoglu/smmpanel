package com.thelastcodebenders.follower.blog.repository;

import com.thelastcodebenders.follower.blog.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
}
