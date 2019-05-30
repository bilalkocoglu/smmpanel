package com.thelastcodebenders.follower.service;

import com.thelastcodebenders.follower.assembler.ServiceAssembler;
import com.thelastcodebenders.follower.dto.ServiceFormDTO;
import com.thelastcodebenders.follower.dto.UserPageServiceDTO;
import com.thelastcodebenders.follower.dto.VisitorServicesItem;
import com.thelastcodebenders.follower.dto.userservices.UserServicesListItem;
import com.thelastcodebenders.follower.dto.userservices.UserServicesListSubItem;
import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.exception.DetectedException;
import com.thelastcodebenders.follower.model.*;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.repository.PackageRepository;
import com.thelastcodebenders.follower.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.springframework.stereotype.Service
public class ServiceService {
    private static final Logger log = LoggerFactory.getLogger(ServiceService.class);

    private ServiceRepository serviceRepository;
    private ServiceAssembler serviceAssembler;
    private CategoryService categoryService;
    private PackageRepository packageRepository;
    private DrawPrizeService drawPrizeService;

    public ServiceService(ServiceRepository serviceRepository,
                          ServiceAssembler serviceAssembler,
                          CategoryService categoryService,
                          PackageRepository packageRepository,
                          DrawPrizeService drawPrizeService){
        this.serviceRepository = serviceRepository;
        this.serviceAssembler = serviceAssembler;
        this.categoryService = categoryService;
        this.packageRepository = packageRepository;
        this.drawPrizeService = drawPrizeService;
    }


    public int activeServiceCount(){
        return (int)serviceRepository.countByState(ServiceState.ACTIVE);
    }

    public List<String> serviceColumns(){
        return Stream.of(
                "ID",
                "API ID",
                "Name",
                "Category",
                "API",
                "Min",
                "Max",
                "Api Price",
                "Custom Price",
                "State",
                "Action").collect(Collectors.toList());
    }



    public List<Service> findAll(){
        return serviceRepository.findAll();
    }

    public Service findServiceById(long serviceId){
        try {
            Optional<Service> opt = serviceRepository.findById(serviceId);
            if (opt.isPresent())
                return opt.get();
            else {
                log.error("Service Service Find By Id Error");
                throw new DetectedException("Servis bulunamadı !");
            }
        }catch (Exception e){
            if ((e instanceof DetectedException)){
                throw e;
            }
            log.error("Service Service Find By Id Error -> " + e.getMessage());
            return null;
        }
    }

    public List<Service> findActiveServiceBySubCategoryId(long subctgId){
        try {
            SubCategory subCategory = categoryService.findSubCategoryById(subctgId);
            List<Service> services = serviceRepository.findBySubCategory(subCategory);
            List<Service> res = new ArrayList<>();
            for (Service s: services) {
                if (s.getState() == ServiceState.ACTIVE)
                    res.add(s);
            }
            return res;
        }catch (Exception e){
            log.error("Service Service Find By Category Error -> " + e.getMessage());
            return null;
        }

    }

    public List<Service> findActiveServiceBySubCategory(SubCategory subCategory){
        return serviceRepository.findBySubCategoryAndState(subCategory, ServiceState.ACTIVE);
    }



    public ServiceFormDTO createFormDto(Service service){
        return serviceAssembler.convertServiceToFormDto(service);
    }

    public boolean formDtoIsValidate(ServiceFormDTO serviceForm){
        if (serviceForm.getCustomPrice() > 0 && serviceForm.getCustomMax() > 0 &&
                serviceForm.getCustomMax() > 0 && serviceForm.getCustomMax() >= serviceForm.getCustomMin()){
            return true;
        }else {
            return false;
        }
    }

    public boolean updateService(ServiceFormDTO serviceForm, long serviceId){
        try {
            if (formDtoIsValidate(serviceForm)){
                Service service = findServiceById(serviceId);
                if (service == null)
                    return false;

                if (serviceForm.getCustomMin() < service.getApiMinPiece() || serviceForm.getCustomMax()>service.getApiMaxPiece()){
                    throw new DetectedException("Max-Min değerleri belirtilen aralıkta olmak zorundadır !");
                }

                if (service.getState() == ServiceState.DELETED){
                    throw new DetectedException("Silinmiş bir servisi güncelleyemezsiniz !");
                }

                if (serviceForm.isActive() &&
                        (service.getState() == ServiceState.PASSIVE || service.getState() == ServiceState.DELETED) &&
                                !service.getApi().isState()){
                    throw new DetectedException("Aktif etmek istediğiniz servisin bağlı olduğu API pasif durumda !");
                }

                if (!serviceForm.isActive() && service.getState() == ServiceState.ACTIVE){

                    //packages
                    List<Package> packages = packageRepository.findByService(service);
                    for (Package pkg: packages) {
                        pkg.setState(false);
                    }
                    packageRepository.saveAll(packages);

                    //drawprizes
                    drawPrizeService.servicePassivateHandler(service);
                }

                SubCategory subCategory = categoryService.findSubCategoryById(serviceForm.getSubcategoryId());
                if (subCategory == null)
                    return false;

                service = serviceAssembler.convertFormDtoToService(serviceForm, service, subCategory);
                serviceRepository.save(service);
                return true;
            }else {
                throw new DetectedException("Hatalı veri girdiniz. Bu şekilde güncelleme yapamazsınız !");
            }
        }catch (Exception e){
            if (e instanceof DetectedException){
                throw e;
            }
            log.error("Service Service Update Error -> " + e.getMessage());
            return false;
        }
    }


    @Cacheable("service-userpage")
    public List<UserServicesListItem> createUserServicesItems(){
        try {
            List<UserServicesListItem> userServicesListItems = new ArrayList<>();
            List<Category> categories = categoryService.allCategory();
            for (Category category: categories) {
                UserServicesListItem userServicesListItem = new UserServicesListItem();
                userServicesListItem.setCategory(category);     //tüm kategoriler için bir item oluşturulur



                /*
                    bu kategoriye ait aktif bir kategori mevcutsa
                    Kapmanyalar adında bir listsubitem oluşturulup userServicesListSubItems içine atılır
                    bu item in services özelliğine paketler atılır.
                 */
                List<Package> packages = packageRepository.findByCategoryAndState(category, true, new Sort(Sort.Direction.ASC, "quantity"));
                if (!packages.isEmpty())
                    userServicesListItem.setPackages(packages);
                else
                    userServicesListItem.setPackages(new ArrayList<>());


                List<UserServicesListSubItem> userServicesListSubItems = new ArrayList<>();

                List<SubCategory> subCategories = categoryService.findSubCategoryByMainCategoryId(category.getId());
                for (SubCategory subcategory: subCategories) {
                    //bütün alt kategoriler için birer servicesubitem oluşturulur ve içine servisler atılır
                    UserServicesListSubItem userServicesListSubItem = new UserServicesListSubItem();
                    userServicesListSubItem.setSubCategory(subcategory);

                    List<Service> services = findActiveServiceBySubCategoryId(subcategory.getId());
                    userServicesListSubItem.setServices(services);
                    if (userServicesListSubItem.getServices().size()>0)
                        userServicesListSubItems.add(userServicesListSubItem);
                }
                userServicesListItem.setSubItems(userServicesListSubItems);
                if (userServicesListItem.getSubItems().size()>0)
                    userServicesListItems.add(userServicesListItem);
            }
            userServicesListItems.get(0).setFirst(true);
            return userServicesListItems;
        }catch (Exception e){
            log.error("Service Service Create User Services Error -> " + e.getMessage());
            return null;
        }
    }

    @Cacheable("service-visitor")
    public List<VisitorServicesItem> createVisitorServicesItems(){
        List<VisitorServicesItem> visitorServicesItems = new ArrayList<>();
        List<Category> categories = categoryService.allCategory();


        categories.forEach(category -> {
            List<SubCategory> subCategories = categoryService.findSubCategoryByMainCategory(category);

            List<Service> services = new ArrayList<>();

            subCategories.forEach(subCategory -> {
               List<Service> subCategoryServices = findActiveServiceBySubCategory(subCategory);
               services.addAll(subCategoryServices);
            });

            if (services.size()>0){
                visitorServicesItems.add(VisitorServicesItem.builder().category(category).services(services).build());
            }
        });

        return visitorServicesItems;
    }

    public UserPageServiceDTO createUserpageServiceAjaxFormat(long serviceId){
        try {
            Service service = findServiceById(serviceId);

            if (service == null)
                return null;

            return serviceAssembler.convertServiceToUserPageService(service);
        }catch (Exception e){
            log.error("Service Service Create User Page Service Format Error -> " + e.getMessage());
            return null;
        }
    }
}
