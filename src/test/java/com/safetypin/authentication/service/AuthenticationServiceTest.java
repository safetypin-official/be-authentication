package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.dto.SocialLoginRequest;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OTPService otpService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private final String JWT_SECRET_KEY = "5047c55bfe120155fd4e884845682bb8b8815c0048a686cc664d1ea6c8e094da";
    private final long EXPIRATION_TIME = 86400000;

    // registerUser tests

    @Test
    void testRegisterUser_UnderAge() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");
        // set birthdate to 17 years old
        request.setBirthdate(LocalDate.now().minusYears(15));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.registerUser(request)
        );
        assertEquals("User must be at least 16 years old", exception.getMessage());
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");
        request.setBirthdate(LocalDate.now().minusYears(20));

        when(userRepository.findByEmail("test@example.com")).thenReturn(new User());

        Exception exception = assertThrows(UserAlreadyExistsException.class, () ->
                authenticationService.registerUser(request)
        );
        assertTrue(exception.getMessage().contains("Email address is already registered"));
    }

    @Test
    void testRegisterUser_Success() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");
        request.setBirthdate(LocalDate.now().minusYears(20));

        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        User savedUser = new User();
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setName("Test User");
        savedUser.setVerified(false);
        savedUser.setRole("USER");
        savedUser.setBirthdate(request.getBirthdate());
        savedUser.setProvider("EMAIL");
        savedUser.setSocialId(null);

        UUID id = UUID.randomUUID();
        savedUser.setId(id);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findById(id)).thenReturn(Optional.of(savedUser));

        String token = authenticationService.registerUser(request);
        assertNotNull(token);
        UserResponse userResponse = authenticationService.getUserFromJwtToken(token);
        assertEquals("test@example.com", userResponse.getEmail());
        // OTPService should be invoked to generate OTP.
        verify(otpService, times(1)).generateOTP("test@example.com");
    }

    // socialLogin tests

    @Test
    void testSocialLogin_UnderAge() {
        SocialLoginRequest request = new SocialLoginRequest();
        request.setEmail("social@example.com");
        request.setName("Social User");
        request.setBirthdate(LocalDate.now().minusYears(15));
        request.setProvider("GOOGLE");
        request.setSocialId("social123");
        request.setSocialToken("token");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.socialLogin(request)
        );
        assertEquals("User must be at least 16 years old", exception.getMessage());
    }

    @Test
    void testSocialLogin_DuplicateEmailWithEmailProvider() {
        SocialLoginRequest request = new SocialLoginRequest();
        request.setEmail("social@example.com");
        request.setName("Social User");
        request.setBirthdate(LocalDate.now().minusYears(25));
        request.setProvider("APPLE");
        request.setSocialId("social123");
        request.setSocialToken("token");

        User existingUser = new User();
        existingUser.setEmail("social@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setName("Existing User");
        existingUser.setVerified(false);
        existingUser.setRole("USER");
        existingUser.setBirthdate(LocalDate.now().minusYears(30));
        existingUser.setProvider("EMAIL");
        existingUser.setSocialId(null);

        when(userRepository.findByEmail("social@example.com")).thenReturn(existingUser);

        Exception exception = assertThrows(UserAlreadyExistsException.class, () ->
                authenticationService.socialLogin(request)
        );
        assertTrue(exception.getMessage().contains("An account with this email exists"));
    }

    @Test
    void testSocialLogin_ExistingSocialUser() {
        SocialLoginRequest request = new SocialLoginRequest();
        request.setEmail("social@example.com");
        request.setName("Social User");
        request.setBirthdate(LocalDate.now().minusYears(25));
        request.setProvider("GOOGLE");
        request.setSocialId("social123");
        request.setSocialToken("token");

        User existingUser = new User();
        existingUser.setEmail("social@example.com");
        existingUser.setPassword(null);
        existingUser.setName("Social User");
        existingUser.setVerified(true);
        existingUser.setRole("USER");
        existingUser.setBirthdate(LocalDate.now().minusYears(25));
        existingUser.setProvider("GOOGLE");
        existingUser.setSocialId("social123");
        UUID id = UUID.randomUUID();
        existingUser.setId(id);

        when(userRepository.findByEmail("social@example.com")).thenReturn(existingUser);
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        String token = authenticationService.socialLogin(request);
        assertNotNull(token);
        UserResponse userResponse = authenticationService.getUserFromJwtToken(token);

        assertEquals("social@example.com", userResponse.getEmail());
    }

    @Test
    void testSocialLogin_NewUser() {
        SocialLoginRequest request = new SocialLoginRequest();
        request.setEmail("social@example.com");
        request.setName("Social User");
        request.setBirthdate(LocalDate.now().minusYears(25));
        request.setProvider("GOOGLE");
        request.setSocialId("social123");
        request.setSocialToken("token");

        when(userRepository.findByEmail("social@example.com")).thenReturn(null);
        User savedUser = new User();
        savedUser.setEmail("social@example.com");
        savedUser.setPassword(null);
        savedUser.setName("Social User");
        savedUser.setVerified(true);
        savedUser.setRole("USER");
        savedUser.setBirthdate(request.getBirthdate());
        savedUser.setProvider("GOOGLE");
        savedUser.setSocialId("social123");

        UUID id = UUID.randomUUID();
        savedUser.setId(id);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findById(id)).thenReturn(Optional.of(savedUser));

        String token = authenticationService.socialLogin(request);
        assertNotNull(token);
        UserResponse userResponse = authenticationService.getUserFromJwtToken(token);
        assertEquals("social@example.com", userResponse.getEmail());
    }

    // loginUser tests

    @Test
    void testLoginUser_EmailNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(null);
        Exception exception = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.loginUser("notfound@example.com", "password")
        );
        assertTrue(exception.getMessage().contains("Invalid email"));
    }

    @Test
    void testLoginUser_InvalidPassword_NullPassword() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(null);
        user.setName("Test User");
        user.setVerified(true);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");
        user.setSocialId(null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        Exception exception = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.loginUser("test@example.com", "password")
        );
        assertTrue(exception.getMessage().contains("Invalid password"));
    }

    @Test
    void testLoginUser_InvalidPassword_WrongMatch() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(true);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");
        user.setSocialId(null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        Exception exception = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.loginUser("test@example.com", "wrongPassword")
        );
        assertTrue(exception.getMessage().contains("Invalid password"));
    }

    @Test
    void testLoginUser_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(true);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");
        user.setSocialId(null);

        UUID id = UUID.randomUUID();
        user.setId(id
        );
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        String token = authenticationService.loginUser("test@example.com", "password");
        assertNotNull(token);
        UserResponse userResponse = authenticationService.getUserFromJwtToken(token);
        assertEquals("test@example.com", userResponse.getEmail());
    }

    // loginSocial tests

    @Test
    void testLoginSocial_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(null);
        Exception exception = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.loginSocial("notfound@example.com")
        );
        assertTrue(exception.getMessage().contains("Social login failed"));
    }

    @Test
    void testLoginSocial_Success() {
        User user = new User();
        user.setEmail("social@example.com");
        user.setPassword(null);
        user.setName("Social User");
        user.setVerified(true);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(25));
        user.setProvider("GOOGLE");
        user.setSocialId("social123");

        UUID id = UUID.randomUUID();
        user.setId(id);

        when(userRepository.findByEmail("social@example.com")).thenReturn(user);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        String token =authenticationService.loginSocial("social@example.com");
        assertNotNull(token);
        UserResponse userResponse = authenticationService.getUserFromJwtToken(token);
        assertEquals("social@example.com", userResponse.getEmail());

    }

    // verifyOTP tests

    @Test
    void testVerifyOTP_Success() {
        // OTPService returns true and user is found
        when(otpService.verifyOTP("test@example.com", "123456")).thenReturn(true);
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(false);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");
        user.setSocialId(null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = authenticationService.verifyOTP("test@example.com", "123456");
        assertTrue(result);
        assertTrue(user.isVerified());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testVerifyOTP_Success_UserNotFound() {
        // OTPService returns true but user is not found
        when(otpService.verifyOTP("nonexistent@example.com", "123456")).thenReturn(true);
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        boolean result = authenticationService.verifyOTP("nonexistent@example.com", "123456");
        assertTrue(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyOTP_Failure() {
        when(otpService.verifyOTP("test@example.com", "000000")).thenReturn(false);
        boolean result = authenticationService.verifyOTP("test@example.com", "000000");
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    // forgotPassword tests

    @Test
    void testForgotPassword_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(true);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");
        user.setSocialId(null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        assertDoesNotThrow(() -> authenticationService.forgotPassword("test@example.com"));
    }

    @Test
    void testForgotPassword_Invalid() {
        // Case 1: user not found
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(null);
        Exception exception1 = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.forgotPassword("notfound@example.com")
        );
        assertTrue(exception1.getMessage().contains("Password reset is only available for email-registered users."));

        // Case 2: user exists but provider is not EMAIL
        User user = new User();
        user.setEmail("social@example.com");
        user.setPassword(null);
        user.setName("Social User");
        user.setVerified(true);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(25));
        user.setProvider("GOOGLE");
        user.setSocialId("social123");

        when(userRepository.findByEmail("social@example.com")).thenReturn(user);
        Exception exception2 = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.forgotPassword("social@example.com")
        );
        assertTrue(exception2.getMessage().contains("Password reset is only available for email-registered users."));
    }

    // postContent tests

    @Test
    void testPostContent_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(null);
        String response = authenticationService.postContent("notfound@example.com", "Content");
        assertEquals("User not found. Please register.", response);
    }

    @Test
    void testPostContent_UserNotVerified() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(false);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");
        user.setSocialId(null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        String response = authenticationService.postContent("test@example.com", "Content");
        assertTrue(response.contains("not verified"));
    }

    @Test
    void testPostContent_UserVerified() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(true);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");
        user.setSocialId(null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        String response = authenticationService.postContent("test@example.com", "Content");
        assertEquals("Content posted successfully", response);
    }
}
