package com.thelastcodebenders.follower.blog.assembler;

import com.thelastcodebenders.follower.blog.dto.PostForm;
import com.thelastcodebenders.follower.blog.enums.PostType;
import com.thelastcodebenders.follower.blog.model.Post;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.service.CategoryService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


@Component
public class PostAssembler {

    private CategoryService categoryService;

    public PostAssembler(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    public Post convertFormToPost(PostForm form){
        Post post = new Post();
        if (form.getPostId()!=null)
            post.setId(Long.valueOf(form.getPostId()));
        post.setBody(form.getBody());

        Category category = categoryService.findCategoryById(Long.valueOf(form.getCategoryId()));
        if (category == null){
            throw new DetectedException("Böyle bir kategori bulunamadı.");
        }

        post.setCategory(category);

        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        //time zone
        post.setDate(LocalDateTime.now().plusHours(3).format(customFormatter));
        post.setMainPhotoUrl(form.getMainPhotoUrl());
        post.setTags(form.getTags());
        post.setViewCount((long)0);
        post.setType(PostType.DRAFT);
        post.setTitle(form.getTitle());
        post.setSummary(form.getSummary());
        return post;
    }

    public PostForm convertPostToForm(Post post){
        PostForm postForm = PostForm.builder()
                .body(post.getBody())
                .categoryId(String.valueOf(post.getCategory().getId()))
                .mainPhotoUrl(post.getMainPhotoUrl())
                .postId(String.valueOf(post.getId()))
                .tags(post.getTags())
                .title(post.getTitle())
                .summary(post.getSummary())
                .build();
        return postForm;
    }
}
