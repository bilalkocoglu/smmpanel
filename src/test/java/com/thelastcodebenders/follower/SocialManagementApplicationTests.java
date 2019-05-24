package com.thelastcodebenders.follower;

import com.thelastcodebenders.follower.model.API;
import com.thelastcodebenders.follower.model.Service;
import com.thelastcodebenders.follower.repository.APIRepository;
import com.thelastcodebenders.follower.repository.ServiceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SocialManagementApplicationTests {

    @Autowired
    APIRepository apiRepository;

    @Autowired
    ServiceRepository serviceRepository;

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

  /*
    @Test
    public void name() {
        Role admin = Role.builder().role("ADMIN").build();
        Role user = Role.builder().role("USER").build();

        admin = roleRepository.save(admin);
        user = roleRepository.save(user);

        User user1 = User.builder()
                .name("Bilal")
                .balance(0)
                .mail("sosyal1trend@gmail.com")
                .number("5347756260")
                .password(bCryptPasswordEncoder.encode("socialtrend.adminbilal"))
                .role(admin)
                .state(true)
                .surname("Koçoğlu")
                .build();

        user1 = userRepository.save(user1);

        System.out.println(user1);
    }

    @Test
    public void apiUpdateServices() {
        apiService.allApiUpdateOtherService();
    }
    @Test
    public void deleteServicesById(){
        API api = apiRepository.findById((long)1293).get();

        List<Service> services;
        services = serviceRepository.findByApi(api);

        System.out.println(services.size());

        serviceRepository.deleteAll(services);
    }
*/


}

