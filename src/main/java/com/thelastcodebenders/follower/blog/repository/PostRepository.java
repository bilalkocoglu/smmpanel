package com.thelastcodebenders.follower.blog.repository;

import com.thelastcodebenders.follower.blog.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    int countBySlug(String slug);

    List<Post> findBySlug(String slug);
}
