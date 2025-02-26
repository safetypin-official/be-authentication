package com.safetypin.authentication.seeder;

import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile({"dev"})
public class DevDataSeeder implements Runnable {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        run();
    }

    @Override
    public void run() {
        // Only seed if there are no users in the repository
        if (userRepository.count() == 0) {
            userRepository.save(new User("user1@example.com",
                    passwordEncoder.encode("password1"),
                    "User One",
                    true,
                    "user",
                    LocalDate.of(1990, 1, 1),
                    "EMAIL",
                    "social1"));

            userRepository.save(new User("user2@example.com",
                    passwordEncoder.encode("password2"),
                    "User Two",
                    true,
                    "user",
                    LocalDate.of(1991, 2, 2),
                    "EMAIL",
                    "social2"));

            userRepository.save(new User("user3@example.com",
                    passwordEncoder.encode("password3"),
                    "User Three",
                    true,
                    "user",
                    LocalDate.of(1992, 3, 3),
                    "EMAIL",
                    "social3"));

            userRepository.save(new User("user4@example.com",
                    passwordEncoder.encode("password4"),
                    "User Four",
                    true,
                    "user",
                    LocalDate.of(1993, 4, 4),
                    "EMAIL",
                    "social4"));

            userRepository.save(new User("user5@example.com",
                    passwordEncoder.encode("password5"),
                    "User Five",
                    true,
                    "user",
                    LocalDate.of(1994, 5, 5),
                    "EMAIL",
                    "social5"));
        }
    }
}
