package com.thelastcodebenders.follower.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostForm {
    private String postId;
    private String categoryId;
    private String body;
    private String title;
    private String mainPhotoUrl;
    private String tags;
    private String summary;
}
