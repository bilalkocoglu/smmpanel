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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SocialManagementApplicationTests {

    @Test
    public void time() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now);
        LocalDateTime timeOutDate = LocalDateTime.now().minus(30, ChronoUnit.MINUTES);
        System.out.println(timeOutDate);

        LocalDateTime messageDate = LocalDateTime.parse("2019-05-02T18:04:59");
        LocalDateTime messageDate1 = LocalDateTime.parse("2019-05-02T18:44:59");

        System.out.println(messageDate);
        System.out.println(messageDate.compareTo(timeOutDate));
        System.out.println(messageDate.compareTo(messageDate));
        System.out.println(messageDate1.compareTo(timeOutDate));
    }
}

