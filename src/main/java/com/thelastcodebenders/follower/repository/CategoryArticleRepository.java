package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.model.CategoryArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryArticleRepository extends JpaRepository<CategoryArticle, Long> {
    int countByCategory(Category category);

    List<CategoryArticle> findByCategory(Category category);
}
