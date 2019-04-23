package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByRole(String role);
}
