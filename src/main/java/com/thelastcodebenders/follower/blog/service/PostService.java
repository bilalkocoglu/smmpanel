package com.thelastcodebenders.follower.blog.service;

import com.thelastcodebenders.follower.blog.assembler.PostAssembler;
import com.thelastcodebenders.follower.blog.dto.PostForm;
import com.thelastcodebenders.follower.blog.enums.FindSlugType;
import com.thelastcodebenders.follower.blog.enums.PostType;
import com.thelastcodebenders.follower.blog.model.Post;
import com.thelastcodebenders.follower.blog.repository.PostRepository;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private static final int POST_PERIOD = 5;
    private static final int HOT_NEWS_COUNT = 3;

    private PostRepository postRepository;
    private PostAssembler postAssembler;
    private CategoryService categoryService;

    public PostService(PostRepository postRepository,
                       PostAssembler postAssembler,
                       CategoryService categoryService){
        this.postRepository = postRepository;
        this.postAssembler = postAssembler;
        this.categoryService = categoryService;
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

    public Post findPostBySlug(String slug){
        List<Post> posts = postRepository.findBySlug(slug);

        if (posts.isEmpty())
            throw new DetectedException("Malesef yazı bulunamadı!");
        else
            return posts.get(0);
    }

    //admin

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

    //visitor

    public HashSet<Category> postCategories(){
        HashSet<Category> categories = new HashSet<>();

        findAll().forEach(post -> {
            if (post.getType() == PostType.PUBLISHED)
                categories.add(post.getCategory());
        });

        return categories;
    }

    public List<Integer> getPageNumbers(){
        int count = getPageCount(postRepository.findByType(PostType.PUBLISHED), POST_PERIOD);

        List<Integer> pages = new ArrayList<>();

        if (count > 0){
            for (int i=1; i<=count; i++)
                pages.add(i);
        }

        return pages;
    }

    private int getPageCount(List<Post> posts, int period){
        if (posts.size() == 0)
            return 0;

        int count = posts.size() / period;

        if (posts.size() % period != 0)
            count+=1;

        return count;
    }

    public int pageNumberControl(int page, int maxPage){
        if (page < 1)
            page = 1;
        else if (page > maxPage)
            page = maxPage;

        return page;
    }

    public List<Post> getPosts(int page){
        Pageable pageable = PageRequest.of((page-1), POST_PERIOD);
        Page<Post> posts = postRepository.findByTypeOrderByIdDesc(PostType.PUBLISHED, pageable);

        return posts.getContent();
    }

    public List<Post> getHotNews(){
        Pageable pageable = PageRequest.of(0, HOT_NEWS_COUNT);
        Page<Post> posts = postRepository.findByTypeOrderByIdDesc(PostType.PUBLISHED, pageable);

        return posts.getContent();
    }

    public List<Post> getPopulars(){
        return postRepository.findTop3ByTypeOrderByViewCountDesc(PostType.PUBLISHED);
    }

    @Async
    public void postViewCountPlus(Post post){
        post.setViewCount(post.getViewCount()+1);
        postRepository.save(post);
    }

    public List<Post> getSimilarPosts(Post post){
        List<Post> similarPosts = new ArrayList<>();
        List<Post> posts = postRepository.findByCategoryAndType(post.getCategory(), PostType.PUBLISHED);

        for (Post p: posts ) {
            if (similarPosts.size()<4 && !p.equals(post))
                similarPosts.add(p);
        }

        return similarPosts;
    }

    public String nextPrevPostSlug(String currentSlug, FindSlugType type){
        Post post = findPostBySlug(currentSlug);

        List<Post> posts = postRepository.findByType(PostType.PUBLISHED);

        for (int i=0; i < posts.size(); i++){
            if (posts.get(i).equals(post)){
                if (type == FindSlugType.NEXT){
                    if ((i+1)>=posts.size())
                        return posts.get(posts.size()-1).getSlug();
                    else
                        return posts.get(i+1).getSlug();
                }else if (type == FindSlugType.PREV){
                    if ((i-1)<0)
                        return posts.get(0).getSlug();
                    else
                        return posts.get(i-1).getSlug();
                }
            }
        }
        return currentSlug;
    }

    public List<Integer> getCategoryPageNumbers(Category category){
        int count = getPageCount(postRepository.findByTypeAndCategory(PostType.PUBLISHED, category), POST_PERIOD);

        List<Integer> pages = new ArrayList<>();

        if (count > 0){
            for (int i=1; i<=count; i++)
                pages.add(i);
        }

        return pages;
    }

    public List<Post> categorPosts(Category category, int page){
        Pageable pageable = PageRequest.of((page-1), POST_PERIOD);
        Page<Post> postPage = postRepository.findByTypeAndCategoryOrderByIdDesc(PostType.PUBLISHED, category, pageable);

        if (postPage.getContent().isEmpty())
            throw new DetectedException("Böyle bir kategori bulunamadı !");
        else
            return postPage.getContent();
    }
}
