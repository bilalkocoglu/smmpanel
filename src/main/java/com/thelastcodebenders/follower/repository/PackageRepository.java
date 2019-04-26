package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.Category;
import com.thelastcodebenders.follower.model.Package;
import com.thelastcodebenders.follower.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    List<Package> findByService(Service service);

    List<Package> findByCategoryAndState(Category category, boolean state);
}
