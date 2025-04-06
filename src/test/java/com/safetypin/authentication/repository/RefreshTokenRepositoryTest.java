package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.RefreshToken;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    private User registeredUser, premiumUser;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();

        // Create and save users with different roles
        registeredUser = new User();
        registeredUser.setEmail("registered@example.com");
        registeredUser.setPassword("password");
        registeredUser.setName("Registered User 1");
        registeredUser.setRole(Role.REGISTERED_USER);
        userRepository.save(registeredUser);

        premiumUser = new User();
        premiumUser.setEmail("premium@example.com");
        premiumUser.setPassword("password");
        premiumUser.setName("Premium User");
        premiumUser.setRole(Role.PREMIUM_USER);
        userRepository.save(premiumUser);

        // Create Refresh token for first user
        refreshToken = new RefreshToken();
        refreshToken.setUser(registeredUser);
        refreshToken.setToken("Test-token");
        refreshToken.setExpiryTime(Instant.now().plusSeconds(60)); // + 60s
        refreshTokenRepository.save(refreshToken);
    }

    @Test
    void testAutomaticId() {
        // check if id is created when saved
        RefreshToken token = new RefreshToken();
        token.setUser(premiumUser);
        token.setToken("new-token");
        token.setExpiryTime(Instant.now().plusSeconds(60));

        RefreshToken savedToken = refreshTokenRepository.save(token);

        assertNotNull(savedToken.getId());
    }

    @Test
    void testNotUniqueTokenSaved() {
        // check if token unique when saved to database
        RefreshToken duplicateToken = new RefreshToken();
        duplicateToken.setUser(premiumUser);
        duplicateToken.setToken("Test-token"); // Duplicate token
        duplicateToken.setExpiryTime(Instant.now().plusSeconds(60));

        assertThrows(Exception.class,
                () -> refreshTokenRepository.saveAndFlush(duplicateToken));
    }

    @Test
    void testNotUniqueUserSaved() {
        // check if user unique when saved to database
        RefreshToken duplicateToken = new RefreshToken();
        duplicateToken.setUser(registeredUser);
        duplicateToken.setToken("New-token"); // Duplicate token
        duplicateToken.setExpiryTime(Instant.now().plusSeconds(60));

        assertThrows(Exception.class,
                () -> refreshTokenRepository.saveAndFlush(duplicateToken));
    }

    @Test
    void testFindByToken_Exists() {
        // Find refreshTokens by the token
        RefreshToken foundToken = refreshTokenRepository.findByToken("Test-token");

        assertNotNull(foundToken);
        assertEquals("Test-token", foundToken.getToken());
        assertEquals(registeredUser.getId(), foundToken.getUser().getId());
    }

    @Test
    void testFindByUserId_NotExists() {
        // Find refreshTokens by the token
        RefreshToken foundToken = refreshTokenRepository.findByToken("1111111Test-token");
        assertNull(foundToken);
    }

    @Test
    void testFindByUserId_Exists() {
        // Find refreshTokens by the token
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUserId(registeredUser.getId());

        assertTrue(foundToken.isPresent());
        assertEquals("Test-token", foundToken.get().getToken());
        assertEquals(registeredUser.getId(), foundToken.get().getUser().getId());
    }

    @Test
    void testFindByToken_NotExists() {
        // Find refreshTokens by the token
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUserId(premiumUser.getId());

        assertFalse(foundToken.isPresent());
    }

    @Test
    void testDeleteBeforeExpiryDate_AfterAWhile() {
        // Set after token expired
        refreshTokenRepository.deleteAllByExpiryTimeBefore(Instant.now().plusSeconds(400));
        assertFalse(refreshTokenRepository.findById(refreshToken.getId()).isPresent());
    }

    @Test
    void testDeleteBeforeExpiryDate_Immediately() {
        // Set before token expired
        refreshTokenRepository.deleteAllByExpiryTimeBefore(Instant.now());
        assertTrue(refreshTokenRepository.findById(refreshToken.getId()).isPresent());
    }
}
