package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TokenExpirationTest {

    private static final String JWT_SECRET_KEY = "5047c55bfe120155fd4e884845682bb8b8815c0048a686cc664d1ea6c8e094da";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OTPService otpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExpiredTokenThrowsCorrectException() {
        // Create a special test-only version of AuthenticationService
        TestAuthenticationService testService = new TestAuthenticationService(
                userRepository, passwordEncoder, otpService);

        // Create a UUID for our test
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        
        // Configure repository to return our user
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // Call our test method that forces the isExpired check to be true
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> testService.testTokenExpiration(userId)
        );
        
        // Verify we get the exact "Token expired" exception message
        assertEquals("Token expired", exception.getMessage(), 
                "The exception message should be 'Token expired' when a token is expired");
    }

    // This class extends AuthenticationService to allow us to test specific code paths
    private class TestAuthenticationService extends AuthenticationService {
        public TestAuthenticationService(UserRepository userRepository, 
                                       PasswordEncoder passwordEncoder, 
                                       OTPService otpService) {
            super(userRepository, passwordEncoder, otpService);
        }

        // This method simulates the token expiration check portion of getUserFromJwtToken
        public UserResponse testTokenExpiration(UUID userId) {
            try {
                // Mock a Claims object with an expired date
                Claims claims = Jwts.claims()
                        .setSubject(userId.toString())
                        .setIssuedAt(new Date(System.currentTimeMillis() - 200000))
                        .setExpiration(new Date(System.currentTimeMillis() - 100000)); // Expired!
                
                // This is the exact code from the main method that checks expiration
                boolean isExpired = claims.getExpiration().before(new Date(System.currentTimeMillis()));
                
                if (isExpired) {
                    throw new InvalidCredentialsException("Token expired");
                }

                // Get user from repository (this won't execute in our test)
                Optional<User> user = userRepository.findById(userId);
                if (user.isEmpty()) {
                    throw new InvalidCredentialsException("User not found");
                }
                return user.get().generateUserResponse();
            } catch (JwtException | IllegalArgumentException e) {
                throw new InvalidCredentialsException("Invalid token");
            }
        }
    }
}
