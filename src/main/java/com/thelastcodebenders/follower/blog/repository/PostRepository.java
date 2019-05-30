package com.thelastcodebenders.follower.blog.repository;

import com.thelastcodebenders.follower.blog.enums.PostType;
import com.thelastcodebenders.follower.blog.model.Post;
import com.thelastcodebenders.follower.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    int countBySlug(String slug);

    List<Post> findBySlug(String slug);

    List<Post> findByType(PostType type);

    List<Post> findByTypeAndCategory(PostType type, Category category);

    Page<Post> findByTypeOrderByIdDesc(PostType type, Pageable pageable);

    Page<Post> findByTypeAndCategoryOrderByIdDesc(PostType type, Category category, Pageable pageable);

    List<Post> findTop3ByTypeOrderByViewCountDesc(PostType type);

    List<Post> findByCategoryAndType(Category category, PostType type);
}
