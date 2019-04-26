package com.thelastcodebenders.follower.repository;

import com.thelastcodebenders.follower.model.AccountActivation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountActivationRepository extends JpaRepository<AccountActivation, Long> {
    int countBySecretkey(String secretKey);

    List<AccountActivation> findBySecretkey(String secretKey);
}
