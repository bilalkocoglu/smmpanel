package com.thelastcodebenders.follower.blog.service;

import com.thelastcodebenders.follower.blog.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TagService {
    private static final Logger log = LoggerFactory.getLogger(TagService.class);

    private TagRepository tagRepository;

    public TagService(TagRepository tagRepository){
        this.tagRepository = tagRepository;
    }
}
