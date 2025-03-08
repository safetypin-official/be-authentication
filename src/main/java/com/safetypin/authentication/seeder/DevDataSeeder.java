package com.safetypin.authentication.seeder;

import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.safetypin.authentication.service.AuthenticationService.EMAIL_PROVIDER;

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

            User user1 = new User();
            user1.setEmail("user1@example.com");
            user1.setPassword(passwordEncoder.encode("password1")); //NOSONAR
            user1.setName("User One");
            user1.setVerified(true);
            user1.setRole(Role.REGISTERED_USER);
            user1.setBirthdate(LocalDate.of(1990, 1, 1));
            user1.setProvider(EMAIL_PROVIDER);
            userRepository.save(user1);

            User user2 = new User();
            user2.setEmail("user2@example.com");
            user2.setPassword(passwordEncoder.encode("password2")); //NOSONAR
            user2.setName("User Two");
            user2.setVerified(true);
            user2.setRole(Role.REGISTERED_USER);
            user2.setBirthdate(LocalDate.of(1991, 2, 2));
            user2.setProvider(EMAIL_PROVIDER);
            userRepository.save(user2);


            User user3 = new User();
            user3.setEmail("user3@example.com");
            user3.setPassword(passwordEncoder.encode("password3")); //NOSONAR
            user3.setName("User Three");
            user3.setVerified(true);
            user3.setRole(Role.REGISTERED_USER);
            user3.setBirthdate(LocalDate.of(1992, 3, 3));
            user3.setProvider(EMAIL_PROVIDER);
            userRepository.save(user3);

            User user4 = new User();
            user4.setEmail("user4@example.com");
            user4.setPassword(passwordEncoder.encode("password4")); //NOSONAR
            user4.setName("User Four");
            user4.setVerified(true);
            user4.setRole(Role.REGISTERED_USER);
            user4.setBirthdate(LocalDate.of(1993, 4, 4));
            user4.setProvider(EMAIL_PROVIDER);
            userRepository.save(user4);

            User user5 = new User();
            user5.setEmail("user5@example.com");
            user5.setPassword(passwordEncoder.encode("password5")); //NOSONAR
            user5.setName("User Five");
            user5.setVerified(true);
            user5.setRole(Role.PREMIUM_USER);
            user5.setBirthdate(LocalDate.of(1994, 5, 5));
            user5.setProvider(EMAIL_PROVIDER);
            userRepository.save(user5);

            User user6 = new User();
            user6.setEmail("user6@example.com");
            user6.setPassword(passwordEncoder.encode("password6")); //NOSONAR
            user6.setName("User Six");
            user6.setVerified(true);
            user6.setRole(Role.MODERATOR);

        }
    }
}
