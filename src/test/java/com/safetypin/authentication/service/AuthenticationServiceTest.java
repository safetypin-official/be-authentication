package com.safetypin.authentication.service;

import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.dto.SocialLoginRequest;
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

    // registerUser tests

    @Test
    void testRegisterUser_UnderAge() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");
        // set birthdate to 17 years old
        request.setBirthdate(LocalDate.now().minusYears(17));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.registerUser(request)
        );
        assertEquals("User must be at least 18 years old", exception.getMessage());
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
        assertTrue(exception.getMessage().contains("User already exists with this email"));
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
        User savedUser = new User("test@example.com", "encodedPassword", "Test User", false, "USER",
                request.getBirthdate(), "EMAIL", null);
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authenticationService.registerUser(request);
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        // OTPService should be invoked to generate OTP.
        verify(otpService, times(1)).generateOTP("test@example.com");
    }

    // socialLogin tests

    @Test
    void testSocialLogin_UnderAge() {
        SocialLoginRequest request = new SocialLoginRequest();
        request.setEmail("social@example.com");
        request.setName("Social User");
        request.setBirthdate(LocalDate.now().minusYears(17));
        request.setProvider("GOOGLE");
        request.setSocialId("social123");
        request.setSocialToken("token");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationService.socialLogin(request)
        );
        assertEquals("User must be at least 18 years old", exception.getMessage());
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

        User existingUser = new User("social@example.com", "encodedPassword", "Existing User", false, "USER",
                LocalDate.now().minusYears(30), "EMAIL", null);
        when(userRepository.findByEmail("social@example.com")).thenReturn(existingUser);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
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

        User existingUser = new User("social@example.com", null, "Social User", true, "USER",
                LocalDate.now().minusYears(25), "GOOGLE", "social123");
        when(userRepository.findByEmail("social@example.com")).thenReturn(existingUser);

        User result = authenticationService.socialLogin(request);
        assertNotNull(result);
        assertEquals("social@example.com", result.getEmail());
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
        User savedUser = new User("social@example.com", null, "Social User", true, "USER",
                request.getBirthdate(), "GOOGLE", "social123");
        savedUser.setId(2L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authenticationService.socialLogin(request);
        assertNotNull(result);
        assertEquals("social@example.com", result.getEmail());
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
        User user = new User("test@example.com", null, "Test User", true, "USER",
                LocalDate.now().minusYears(20), "EMAIL", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        Exception exception = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.loginUser("test@example.com", "password")
        );
        assertTrue(exception.getMessage().contains("Invalid password"));
    }

    @Test
    void testLoginUser_InvalidPassword_WrongMatch() {
        User user = new User("test@example.com", "encodedPassword", "Test User", true, "USER",
                LocalDate.now().minusYears(20), "EMAIL", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        Exception exception = assertThrows(InvalidCredentialsException.class, () ->
                authenticationService.loginUser("test@example.com", "wrongPassword")
        );
        assertTrue(exception.getMessage().contains("Invalid password"));
    }

    @Test
    void testLoginUser_Success() {
        User user = new User("test@example.com", "encodedPassword", "Test User", true, "USER",
                LocalDate.now().minusYears(20), "EMAIL", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        User result = authenticationService.loginUser("test@example.com", "password");
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
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
        User user = new User("social@example.com", null, "Social User", true, "USER",
                LocalDate.now().minusYears(25), "GOOGLE", "social123");
        when(userRepository.findByEmail("social@example.com")).thenReturn(user);

        User result = authenticationService.loginSocial("social@example.com");
        assertNotNull(result);
        assertEquals("social@example.com", result.getEmail());
    }

    // verifyOTP tests

    @Test
    void testVerifyOTP_Success() {
        // OTPService returns true and user is found
        when(otpService.verifyOTP("test@example.com", "123456")).thenReturn(true);
        User user = new User("test@example.com", "encodedPassword", "Test User", false, "USER",
                LocalDate.now().minusYears(20), "EMAIL", null);
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
        User user = new User("test@example.com", "encodedPassword", "Test User", true, "USER",
                LocalDate.now().minusYears(20), "EMAIL", null);
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
        User user = new User("social@example.com", null, "Social User", true, "USER",
                LocalDate.now().minusYears(25), "GOOGLE", "social123");
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
        User user = new User("test@example.com", "encodedPassword", "Test User", false, "USER",
                LocalDate.now().minusYears(20), "EMAIL", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        String response = authenticationService.postContent("test@example.com", "Content");
        assertTrue(response.contains("not verified"));
    }

    @Test
    void testPostContent_UserVerified() {
        User user = new User("test@example.com", "encodedPassword", "Test User", true, "USER",
                LocalDate.now().minusYears(20), "EMAIL", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        String response = authenticationService.postContent("test@example.com", "Content");
        assertEquals("Content posted successfully", response);
    }
}
