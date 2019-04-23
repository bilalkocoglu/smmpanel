package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.model.SubCategory;
import com.thelastcodebenders.follower.repository.CategoryRepository;
import com.thelastcodebenders.follower.repository.ServiceRepository;
import com.thelastcodebenders.follower.repository.SubCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CategoryService {
    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private CategoryRepository categoryRepository;
    private SubCategoryRepository subCategoryRepository;
    private ServiceRepository serviceRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           SubCategoryRepository subCategoryRepository,
                           ServiceRepository serviceRepository){
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.serviceRepository = serviceRepository;
    }

    public List<String> subCategoryColums(){
        return Stream.of("Category", "Main Category", "Action").collect(Collectors.toList());
    }

    public List<String> categoryColumns(){
        return Stream.of("Id", "Category", "Action").collect(Collectors.toList());
    }

    public List<SubCategory> allSubcategory(){
        return subCategoryRepository.findAll(new Sort(Sort.Direction.ASC, "category"));
    }

    public List<Category> allCategory(){
        return categoryRepository.findAll();
    }

    private boolean isAlreadySubCategory(Category category, String name){
        List<SubCategory> subCategories = subCategoryRepository.findByCategory(category);

        for (SubCategory subCtg: subCategories) {
            if (subCtg.getName().equals(name))
                return true;
        }

        return false;
    }

    public boolean saveSubcategory(SubCategory subCategory, String strId){
        try {
            long mainId = Long.parseLong(strId);
            Category maincategory = categoryRepository.findById(mainId).get();

            if (isAlreadySubCategory(maincategory, subCategory.getName()))
                throw new RuntimeException("Bu isimde bir alt kategori zaten bu ana kategori altında mevcut !");

            subCategory.setCategory(maincategory);
            subCategory = subCategoryRepository.save(subCategory);
            if (subCategory == null){
                log.error("Category Service Save Subcategory Error");
                return false;
            }else
                return true;
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("Category Service Save Subcategory Error - " + e.getMessage());
            return false;
        }
    }

    private boolean isAlreadyCategory(String categoryName){
        List<Category> categories = categoryRepository.findByName(categoryName);

        if (categories.isEmpty())
            return false;
        else
            return true;
    }

    public boolean saveCategory(Category category){
        try {
            if (isAlreadyCategory(category.getName()))
                throw new RuntimeException("Bu isimde bir kategori zaten mevcut !");

            category = categoryRepository.save(category);
            if (category==null){
                log.error("Category Service Save Category Error");
                return false;
            }else
                return true;
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("Category Service Save Category Error - " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSubcategory(long subcategoryId){
        try {
            SubCategory subCategory = subCategoryRepository.findById(subcategoryId).get();
            List<com.thelastcodebenders.follower.model.Service> services = serviceRepository.findBySubCategory(subCategory);
            if (services.isEmpty()){
                subCategoryRepository.deleteById(subcategoryId);
                return true;
            }else {
                log.error("Category Service Subcategory Delete Error -> Bu kategori altında servisler mevcut !");
                throw new RuntimeException("Bu kategori altında servisler mevcut, bu yüzden silemezsiniz !");
            }
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("Category Service Subcategory Delete Error - " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMainCategory(long maincategoryId){
        try {
            Category category = categoryRepository.findById(maincategoryId).get();
            List<SubCategory> subCategories = subCategoryRepository.findByCategory(category);
            if (subCategories.isEmpty()){
                categoryRepository.deleteById(maincategoryId);
                return true;
            }else {
                log.error("Category Service Category Delete Error -> Bu kategori altında alt kategori mevcut !");
                throw new RuntimeException("Bu ana kategori altında alt kategoriler mevcut, bu yüzden silemezsiniz !");
            }
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("Category Service Category Delete Error - " + e.getMessage());
            return false;
        }
    }

    public SubCategory findSubCategoryById(long subcategoryId){
        try {
            Optional<SubCategory> opt = subCategoryRepository.findById(subcategoryId);
            if (opt.isPresent())
                return opt.get();
            else {
                log.error("Category Service Find Subcategory By Id Error");
                throw new RuntimeException("Atamak istediğiniz kategori bulunamadı !");
            }
        }catch (Exception e){
            if ((e instanceof RuntimeException)){
                throw e;
            }
            log.error("Category Service Find Subcategory By Id Error -> " + e.getMessage());
            return null;
        }
    }

    public List<SubCategory> findSubCategoryByMainCategory(long mainctgId){
        Optional<Category> opt = categoryRepository.findById(mainctgId);
        if (!opt.isPresent()){
            log.error("Category Service Find Subcategory By Main Category Error -> Not Found");
            return null;
        }else {
            return subCategoryRepository.findByCategory(opt.get());
        }
    }
}
