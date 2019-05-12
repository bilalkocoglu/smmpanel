package com.thelastcodebenders.follower.service;


import com.thelastcodebenders.follower.assembler.PackageAssembler;
import com.thelastcodebenders.follower.dto.PackageFormDTO;
import com.thelastcodebenders.follower.dto.UserPagePackageDTO;
import com.thelastcodebenders.follower.dto.VisitorPackagesItem;
import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.enums.UserAction;
import com.thelastcodebenders.follower.model.*;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.repository.PackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.springframework.stereotype.Service
public class PackageService {
    private static final Logger log = LoggerFactory.getLogger(PackageService.class);

    private PackageRepository packageRepository;
    private PackageAssembler packageAssembler;
    private ServiceService serviceService;
    private CategoryService categoryService;

    public PackageService(PackageRepository packageRepository,
                          PackageAssembler packageAssembler,
                          ServiceService serviceService,
                          CategoryService categoryService){
        this.packageRepository = packageRepository;
        this.packageAssembler = packageAssembler;
        this.serviceService = serviceService;
        this.categoryService = categoryService;
    }

    public List<String> packageColumns(){
        return Stream.of("Paket Adı", "Kategori", "Servis", "Miktar", "Fiyat", "Durum", "Action").collect(Collectors.toList());
    }

    public boolean save(PackageFormDTO packageFormDTO){
        try {
            Service service = serviceService.findServiceById(Long.valueOf(packageFormDTO.getServiceId()));
            Package pkg = packageAssembler.convertFormDtoToPackage(packageFormDTO, service);
            packageRepository.save(pkg);
            return true;
        }catch (Exception e){
            log.error("Package Service Save Error -> " + e.getMessage());
            return false;
        }
    }

    public void saveAll(List<Package> packages){
        packageRepository.saveAll(packages);
    }



    public List<Package> findPackageByService(Service service){
        return packageRepository.findByService(service);
    }

    public Package findById(long id){
        Optional<Package> opt = packageRepository.findById(id);

        if (!opt.isPresent()){
            log.error("Package Service FindById Error -> Not Found !");
            return null;
        }

        return opt.get();
    }

    public List<Package> allPackages(){
        return packageRepository.findAll();
    }

    public boolean changeState(long id, UserAction action){
        try {
            Optional<Package> opt = packageRepository.findById(id);

            if (!opt.isPresent()){
                log.error("Package Service Change State Error - Boyle bir paket bulunamadi !");
                return false;
            }

            Package pkg = opt.get();
            if (action == UserAction.ACTIVATE){
                if (pkg.getService().getState()== ServiceState.PASSIVE || pkg.getService().getState()== ServiceState.DELETED){
                    throw new RuntimeException("Aktifleştirmek istediğiniz paketin bağlı olduğu servis pasif !");
                }
                pkg.setState(true);
            }
            if (action == UserAction.PASSIVATE)
                pkg.setState(false);

            pkg = packageRepository.save(pkg);
            return true;
        }catch (Exception e){
            if (e instanceof RuntimeException)
                throw e;
            log.error("Package Service Change State Error - " + e.getMessage());
            return false;
        }
    }

    public boolean isValidatePackageCategory(Category category){
        if (packageRepository.countByCategoryAndState(category, true)>0)
            return true;
        else
            throw new RuntimeException("Geçersiz Kategori !");
    }

    public List<Category> visitorAllPackageCategories(){
        List<Category> categories = categoryService.allCategory();
        List<Category> notEmptyCategories = new ArrayList<>();

        for (Category ctg: categories) {
            if (packageRepository.countByCategoryAndState(ctg, true)>0){
                notEmptyCategories.add(ctg);
            }
        }

        return notEmptyCategories;
    }

    public List<Category> visitorPopularCategories(){
        List<Category> categories = visitorAllPackageCategories();
        List<Category> popularCategories = new ArrayList<>();

        for (Category ctg: categories) {
            if (popularCategories.size()<4){
                popularCategories.add(ctg);
            }else {
                break;
            }
        }
        return popularCategories;
    }

    public UserPagePackageDTO createUserPageServiceFormat(long id){
        Package pkg = findById(id);

        if (pkg == null)
            return null;

        return packageAssembler.convertPackageToUserPage(pkg);
    }

    public List<VisitorPackagesItem> createVisitorPackagesFormat(Category category){
        List<VisitorPackagesItem> visitorPackagesItems = new ArrayList<>();
        List<Package> packages = packageRepository.findByCategoryAndState(category, true);

        List<SubCategory> subCategories = categoryService.findSubCategoryByMainCategory(category.getId());

        if (!subCategories.isEmpty()){
            for (SubCategory subctg: subCategories) {
                List<Package> findedPackage = findPackageBySubCategory(packages, subctg);

                if (!findedPackage.isEmpty()){
                    VisitorPackagesItem visitorPackagesItem = VisitorPackagesItem.builder().subCategory(subctg).isFirst(false).build();

                    List<UserPagePackageDTO> userPagePackageDTOList = new ArrayList<>();

                    for (Package pkg: findedPackage) {
                        userPagePackageDTOList.add(packageAssembler.convertPackageToUserPage(pkg));
                    }
                    visitorPackagesItem.setPackages(userPagePackageDTOList);
                    visitorPackagesItems.add(visitorPackagesItem);
                }
            }
        }

        if (!visitorPackagesItems.isEmpty())
            visitorPackagesItems.get(0).setFirst(true);

        return visitorPackagesItems;
    }

    private List<Package> findPackageBySubCategory(List<Package> packages, SubCategory subCategory){
        List<Package> findedPackages = new ArrayList<>();
        for (Package pkg: packages) {
            if(pkg.getService().getSubCategory() == subCategory)
                findedPackages.add(pkg);
        }
        return findedPackages;
    }
}
