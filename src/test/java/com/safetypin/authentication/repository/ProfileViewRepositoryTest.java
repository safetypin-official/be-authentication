package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.ProfileView;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProfileViewRepositoryTest {

    @Autowired
    private ProfileViewRepository profileViewRepository;

    @Autowired
    private UserRepository userRepository; // Assuming you have a UserRepository

    private User registeredUser, premiumUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        profileViewRepository.deleteAll();

        // Create and save users with different roles
        registeredUser = new User();
        registeredUser.setEmail("registered@example.com");
        registeredUser.setPassword("password");
        registeredUser.setName("Registered User");
        registeredUser.setRole(Role.REGISTERED_USER);
        userRepository.save(registeredUser);

        premiumUser = new User();
        premiumUser.setEmail("premium@example.com");
        premiumUser.setPassword("password");
        premiumUser.setName("Premium User");
        premiumUser.setRole(Role.PREMIUM_USER);
        userRepository.save(premiumUser);

        // Create and save a ProfileView
        ProfileView profileView = new ProfileView();
        profileView.setUser(premiumUser);
        profileView.setViewer(registeredUser);
        profileView.setViewedAt(LocalDate.now());
        profileViewRepository.save(profileView);
    }

    @Test
    void testFindByUser_IdAndViewer_Id() {
        Optional<ProfileView> result = profileViewRepository.findByUser_IdAndViewer_Id(
                premiumUser.getId(), registeredUser.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getUser()).isEqualTo(premiumUser);
        assertThat(result.get().getViewer()).isEqualTo(registeredUser);
    }

    @Test
    void testFindByUser_Id() {
        List<ProfileView> result = profileViewRepository.findByUser_Id(premiumUser.getId());
        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().getUser()).isEqualTo(premiumUser);
    }
}