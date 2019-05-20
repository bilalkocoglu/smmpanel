package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.enums.ServiceState;
import com.thelastcodebenders.follower.model.API;
import com.thelastcodebenders.follower.model.Service;
import com.thelastcodebenders.follower.model.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    long countByState(ServiceState state);

    List<Service> findBySubCategory(SubCategory subCategory);

    List<Service> findByApi(API api);

    List<Service> findByState(ServiceState state);

    List<Service> findBySubCategoryAndState(SubCategory subCategory, ServiceState state);
}
