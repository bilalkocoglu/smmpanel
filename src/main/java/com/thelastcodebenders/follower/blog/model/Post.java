package com.thelastcodebenders.follower.blog.model;

import com.thelastcodebenders.follower.blog.enums.PostType;
import com.thelastcodebenders.follower.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "post_id")
    private Long id;

    @Column(length = 10000)
    private String body;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "post_tag", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    private String title;
    private String mainPhotoUrl;
    private Long viewCount;
    private PostType type;

    @ManyToOne
    @JoinColumn(name = "fk_category")
    private Category category;
}
