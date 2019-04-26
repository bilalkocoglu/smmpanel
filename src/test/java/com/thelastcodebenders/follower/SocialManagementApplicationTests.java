package com.thelastcodebenders.follower;

import com.thelastcodebenders.follower.model.User;
import com.thelastcodebenders.follower.repository.UserRepository;
import com.thelastcodebenders.follower.service.AccountActivationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SocialManagementApplicationTests {
    @Autowired
    AccountActivationService accountActivationService;

    @Test
    public void pintest() {
        System.out.println(accountActivationService.generateKey());
    }
}

