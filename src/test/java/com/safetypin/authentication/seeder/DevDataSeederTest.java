package com.safetypin.authentication.seeder;

import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("dev")  // Use the 'dev' profile during tests
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class DevDataSeederTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Test that the seeder inserts 5 users when no users exist
    @Test
    void testSeederInsertsUsersWhenEmpty() {
        userRepository.deleteAll(); // Ensure the database is empty before seeding

        new DevDataSeeder(userRepository, passwordEncoder).run(); // Run the seeder

        List<User> users = userRepository.findAll();
        assertEquals(5, users.size(), "Seeder should insert 5 users when repository is empty");
    }

    // Test that the seeder does not add any users if at least one user already exists
    @Test
    void testSeederDoesNotInsertIfUsersExist() {
        // Save an existing user into the repository
        User user = new User();
        user.setEmail("existing@example.com");
        user.setPassword(passwordEncoder.encode("test"));
        user.setName("Existing User");
        user.setVerified(true);
        user.setRole(Role.MODERATOR);
        user.setBirthdate(LocalDate.of(1990, 1, 1));
        user.setProvider("EMAIL");
        userRepository.save(user);

        long countBefore = userRepository.count();
        new DevDataSeeder(userRepository, passwordEncoder).run();
        long countAfter = userRepository.count();

        assertEquals(countBefore, countAfter, "Seeder should not insert new users if users already exist");
    }
}
