package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.model.CategoryArticle;
import com.thelastcodebenders.follower.repository.CategoryArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryArticleService {
    private static final Logger LOG = LoggerFactory.getLogger(CategoryArticleService.class);

    private CategoryArticleRepository categoryArticleRepository;
    private CategoryService categoryService;

    public CategoryArticleService(CategoryArticleRepository categoryArticleRepository,
                                  CategoryService categoryService){
        this.categoryArticleRepository = categoryArticleRepository;
        this.categoryService = categoryService;
    }

    public List<CategoryArticle> findAll(){
        return categoryArticleRepository.findAll();
    }

    public CategoryArticle findByCategoryId(long categoryId){
        Category category = categoryService.findCategoryById(categoryId);

        if (category == null)
            throw new RuntimeException("Böyle bir kategori bulunamadı !");

        List<CategoryArticle> categoryArticles = categoryArticleRepository.findByCategory(category);

        if (categoryArticles.isEmpty())
            throw new RuntimeException("Böyle bir yazı bulunamadı !");

        return categoryArticles.get(0);
    }

    public List<Category> emptyCategories(){
        List<Category> categories = categoryService.allCategory();
        List<Category> emptyCategories = new ArrayList<>();

        for (Category ctg: categories) {
            if (categoryArticleRepository.countByCategory(ctg)==0)
                emptyCategories.add(ctg);
        }

        return emptyCategories;
    }

    private boolean isValidate(CategoryArticle categoryArticle){
        if (categoryArticle.getCategory()==null || categoryArticle.getArticle()==null)
            return false;
        else
            return true;
    }

    public boolean save(CategoryArticle categoryArticle){
        if (!isValidate(categoryArticle)){
            LOG.error("Category Article Service Save Error => CategoryArticle is Not Validate !");
            throw new RuntimeException("İşleminiz gerçekleştirilemedi !");
        }

        Category category = categoryService.findCategoryByName(categoryArticle.getCategory().getName());

        if (category == null){
            throw new RuntimeException("İşleminiz gerçekleştirilemedi !");
        }

        if (categoryArticleRepository.countByCategory(category)>0){
            LOG.error("Category Article Service Save Error => Already category has got a article !");

        }

        categoryArticle = categoryArticleRepository.save(categoryArticle);

        return true;
    }



}
