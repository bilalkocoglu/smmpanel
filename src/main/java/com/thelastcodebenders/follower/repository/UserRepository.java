package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.Role;
import com.thelastcodebenders.follower.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    long countByRole(Role role);

    List<User> findByRole(Role role);
    List<User> findByMail(String mail);
}
