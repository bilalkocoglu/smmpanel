package com.thelastcodebenders.follower;

import com.thelastcodebenders.follower.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
public class SocialManagementApplication implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(SocialManagementApplication.class);

	@Autowired
	ServiceService serviceService;

	@Autowired
	PackageService packageService;

	@Autowired
	AskedQuestionService askedQuestionService;

	@Autowired
	AnnouncementService announcementService;

	@Autowired
	BankAccountService bankAccountService;

	@Autowired
	OrderService orderService;

	public static void main(String[] args) {
		SpringApplication.run(SocialManagementApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//CACHES
		serviceService.createVisitorServicesItems();
		serviceService.createUserServicesItems();
		packageService.activePackagesTop12();
		askedQuestionService.allAskedQuestions();
		announcementService.findAll();
		bankAccountService.allAccounts();
		orderService.getWinnings();
	}
}
