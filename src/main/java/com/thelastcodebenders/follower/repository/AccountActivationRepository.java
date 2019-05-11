package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.AccountActivation;
import com.thelastcodebenders.follower.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sun.nio.cs.US_ASCII;

import java.util.List;

@Repository
public interface AccountActivationRepository extends JpaRepository<AccountActivation, Long> {
    int countBySecretkey(String secretKey);

    List<AccountActivation> findBySecretkey(String secretKey);

    List<AccountActivation> findByUser(User user);
}
