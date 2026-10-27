package com.smartsched;

import com.smartsched.repository.FacultyRepository;
import com.smartsched.service.DemoDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SmartSchedApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartSchedApplication.class, args);
    }

    @Bean
    public CommandLineRunner initialSeed(FacultyRepository facultyRepo, DemoDataService demoService) {
        return args -> {
            if (facultyRepo.count() == 0) {
                demoService.resetAndLoadDemoData("NORMAL");
                System.out.println(">>> SmartSched AI initialized with default University dataset (NORMAL).");
            }
        };
    }
}

