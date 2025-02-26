package com.safetypin.authentication.seeder;

import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile({"dev"})  // Runs only in 'dev' profile
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.saveAll(List.of(
                    new User("alice@example.com", passwordEncoder.encode("password123"), "Alice Johnson", true, "developer",
                            LocalDate.of(1998, 5, 21), "EMAIL", "social_1001"),
                    new User("bob@example.com", passwordEncoder.encode("password456"), "Bob Smith", false, "designer",
                            LocalDate.of(2000, 8, 15), "GOOGLE", "social_1002"),
                    new User("charlie@example.com", passwordEncoder.encode("password789"), "Charlie Davis", true, "manager",
                            LocalDate.of(1995, 12, 3), "APPLE", "social_1003"),
                    new User("diana@example.com", passwordEncoder.encode("password321"), "Diana Roberts", true, "QA engineer",
                            LocalDate.of(2002, 6, 10), "EMAIL", "social_1004"),
                    new User("ethan@example.com", passwordEncoder.encode("password654"), "Ethan Brown", false, "data analyst",
                            LocalDate.of(1999, 11, 27), "EMAIL", "social_1005")
            ));
            System.out.println("Dummy users inserted in DEV environment");
        } else {
            System.out.println("User repo is not empty");
        }


    }
}