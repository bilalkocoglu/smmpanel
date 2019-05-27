package com.thelastcodebenders.follower.blog.service;

import com.thelastcodebenders.follower.blog.model.Post;
import com.thelastcodebenders.follower.blog.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private PostRepository postRepository;

    public PostService(PostRepository postRepository){
        this.postRepository = postRepository;
    }

    public List<Post> findAll(){
        return postRepository.findAll();
    }
}
