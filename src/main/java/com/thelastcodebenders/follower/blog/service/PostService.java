package com.thelastcodebenders.follower.blog.service;

import com.thelastcodebenders.follower.blog.assembler.PostAssembler;
import com.thelastcodebenders.follower.blog.dto.PostForm;
import com.thelastcodebenders.follower.blog.enums.PostType;
import com.thelastcodebenders.follower.blog.model.Post;
import com.thelastcodebenders.follower.blog.repository.PostRepository;
import com.thelastcodebenders.follower.exception.DetectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private PostRepository postRepository;
    private PostAssembler postAssembler;

    public PostService(PostRepository postRepository,
                       PostAssembler postAssembler){
        this.postRepository = postRepository;
        this.postAssembler = postAssembler;
    }

    public List<Post> findAll(){
        return postRepository.findAll();
    }

    public Post findById(long id){
        Optional<Post> opt = postRepository.findById(id);
        if (!opt.isPresent())
            throw new DetectedException("Böyle bir post bulunamadı !");
        return opt.get();
    }

    public PostForm returnPreviewPost(long id){
        Post post = findById(id);
        return postAssembler.convertPostToForm(post);
    }

    public Post previewPost(PostForm form){
        formIsValidate(form);
        Post post = postAssembler.convertFormToPost(form);

        String slug = createSlug(form.getTitle(), post.getId());

        post.setSlug(slug);
        post = postRepository.save(post);
        return post;
    }

    public void deletePost(long postId){
        //daha sonra yorumlarında silinmesi gerek !
        postRepository.deleteById(postId);
    }

    public Post publishPost(long postId){
        Post post = findById(postId);

        post.setType(PostType.PUBLISHED);
        post = postRepository.save(post);
        return post;
    }

    private String createSlug(String title, Long postId) {
        String slug = title.toLowerCase()
                .replace(' ', '-')
                .replace('ı', 'i')
                .replace('ğ', 'g')
                .replace('ü', 'u')
                .replace('ş', 's')
                .replace('ö', 'o')
                .replace('ç', 'c');

        if (postId != null) {
            int count = 1;
            String slugname = slug;
            while (true){
                List<Post> equivalentSlug = postRepository.findBySlug(slug);
                if ((equivalentSlug.size() > 0) && (!equivalentSlug.get(0).getId().equals(postId))){
                    slugname += "-"+String.valueOf(count);
                }else {
                    return slugname;
                }
                count++;
            }
        }else {
            int count = 1;
            String slugname = slug;
            while (true){
                if (postRepository.countBySlug(slugname)>0){
                    slugname += "-"+String.valueOf(count);
                }else {
                    return slugname;
                }
                count++;
            }
        }
    }

    public void formIsValidate(PostForm form){
        if (isNullOrEmpty(form.getBody()) || isNullOrEmpty(form.getCategoryId())
                || isNullOrEmpty(form.getMainPhotoUrl()) || isNullOrEmpty(form.getTags())
                || isNullOrEmpty(form.getTitle()) || isNullOrEmpty(form.getSummary())){
            throw new DetectedException("Tüm alanları eksiksiz doldurmalısınız.");
        }
    }

    private static boolean isNullOrEmpty(String str) {
        if(str != null && !str.isEmpty())
            return false;
        return true;
    }

}
