package com.thelastcodebenders.follower.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "category_article")
public class CategoryArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "category_article_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "fk_category")
    private Category category;

    @Column(length = 10000)
    private String article;
}
