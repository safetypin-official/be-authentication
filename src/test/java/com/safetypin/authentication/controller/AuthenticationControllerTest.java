package com.safetypin.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetypin.authentication.dto.PasswordResetRequest;
import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.dto.SocialLoginRequest;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import({AuthenticationControllerTest.TestConfig.class})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterEmail() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("email@example.com");
        request.setPassword("password");
        request.setName("Test User");
        request.setBirthdate(LocalDate.now().minusYears(20));

        User user = new User();
        user.setEmail("email@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(request.getBirthdate());
        user.setProvider("EMAIL");

        UUID id = UUID.randomUUID();
        user.setId(id);
        String token = authenticationService.generateJwtToken(user.getId());
        Mockito.when(authenticationService.registerUser(any(RegistrationRequest.class))).thenReturn(token);

        mockMvc.perform(post("/api/auth/register-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenValue").value(token));
    }

    @Test
    void testRegisterSocial() throws Exception {
        SocialLoginRequest request = new SocialLoginRequest();
        request.setProvider("GOOGLE");
        request.setSocialToken("token");
        request.setEmail("social@example.com");
        request.setName("Social User");
        request.setBirthdate(LocalDate.now().minusYears(25));
        request.setSocialId("social123");

        User user = new User();
        user.setEmail("social@example.com");
        user.setPassword(null);
        user.setName("Social User");
        user.setVerified(true);
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(request.getBirthdate());
        user.setProvider("GOOGLE");
        user.setSocialId("social123");

        UUID id = UUID.randomUUID();
        user.setId(id);
        String token = authenticationService.generateJwtToken(user.getId());
        Mockito.when(authenticationService.socialLogin(any(SocialLoginRequest.class))).thenReturn(token);

        mockMvc.perform(post("/api/auth/register-social")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenValue").value(token));
    }

    @Test
    void testLoginEmail() throws Exception {
        User user = new User();
        user.setEmail("email@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(true);
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");
        user.setSocialId(null);

        UUID id = UUID.randomUUID();
        user.setId(id);
        String token = authenticationService.generateJwtToken(user.getId());
        Mockito.when(authenticationService.loginUser("email@example.com", "password")).thenReturn(token);

        mockMvc.perform(post("/api/auth/login-email")
                        .param("email", "email@example.com")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenValue").value(token));
    }

    @Test
    void testLoginEmail_InvalidCredentials() throws Exception {
        String errorMessage = "Invalid email or password";
        Mockito.when(authenticationService.loginUser("wrong@example.com", "wrongpassword"))
                .thenThrow(new InvalidCredentialsException(errorMessage));

        mockMvc.perform(post("/api/auth/login-email")
                        .param("email", "wrong@example.com")
                        .param("password", "wrongpassword"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testLoginSocial() throws Exception {
        User user = new User();
        user.setEmail("social@example.com");
        user.setPassword(null);
        user.setName("Social User");
        user.setVerified(true);
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(LocalDate.now().minusYears(25));
        user.setProvider("GOOGLE");
        user.setSocialId("social123");

        UUID id = UUID.randomUUID();
        user.setId(id);
        String token = authenticationService.generateJwtToken(user.getId());
        Mockito.when(authenticationService.loginSocial("social@example.com")).thenReturn(token);

        mockMvc.perform(post("/api/auth/login-social")
                        .param("email", "social@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenValue").value(token));
    }

    @Test
    void testLoginSocial_InvalidCredentials() throws Exception {
        String errorMessage = "User with this email not found";
        Mockito.when(authenticationService.loginSocial("nonexistent@example.com"))
                .thenThrow(new InvalidCredentialsException(errorMessage));

        mockMvc.perform(post("/api/auth/login-social")
                        .param("email", "nonexistent@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testVerifyOTP_Success() throws Exception {
        Mockito.when(authenticationService.verifyOTP("email@example.com", "123456")).thenReturn(true);

        mockMvc.perform(post("/api/auth/verify-otp")
                        .param("email", "email@example.com")
                        .param("otp", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User verified successfully"));
    }

    @Test
    void testVerifyOTP_Failure() throws Exception {
        Mockito.when(authenticationService.verifyOTP("email@example.com", "000000")).thenReturn(false);

        mockMvc.perform(post("/api/auth/verify-otp")
                        .param("email", "email@example.com")
                        .param("otp", "000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("OTP verification failed"));
    }

    @Test
    void testVerifyOTP_InvalidCredentials() throws Exception {
        String errorMessage = "Invalid email or OTP";
        Mockito.when(authenticationService.verifyOTP("email@example.com", "invalid"))
                .thenThrow(new InvalidCredentialsException(errorMessage));

        mockMvc.perform(post("/api/auth/verify-otp")
                        .param("email", "Invalid OTP code or expired"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testForgotPassword() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("email@example.com");

        Mockito.doNothing().when(authenticationService).forgotPassword("email@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset instructions have been sent to your email (simulated)"));
    }

    @Test
    void testVerifyJwtToken_Success() throws Exception {
        String validToken = "valid.jwt.token";
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .isVerified(true)
                .role("REGISTERED_USER")
                .birthdate(LocalDate.now().minusYears(25))
                .provider("EMAIL")
                .build();

        Mockito.when(authenticationService.getUserFromJwtToken(validToken)).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/verify-jwt")
                        .param("token", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    void testVerifyJwtToken_InvalidToken() throws Exception {
        String invalidToken = "invalid.token";
        String errorMessage = "Invalid or expired JWT token";

        Mockito.when(authenticationService.getUserFromJwtToken(invalidToken))
                .thenThrow(new InvalidCredentialsException(errorMessage));

        mockMvc.perform(post("/api/auth/verify-jwt")
                        .param("token", invalidToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testPostContent() throws Exception {
        Mockito.when(authenticationService.postContent("email@example.com", "Test Content"))
                .thenReturn("Content posted successfully");

        mockMvc.perform(post("/api/auth/post")
                        .param("email", "email@example.com")
                        .param("content", "Test Content"))
                .andExpect(status().isOk())
                .andExpect(content().string("Content posted successfully"));
    }

    @Test
    void testDashboard() throws Exception {
        mockMvc.perform(get("/api/auth/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthenticationService authenticationService() {
            return Mockito.mock(AuthenticationService.class);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)  // Appropriate for JWT authentication
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    );
            return http.build();
        }
    }
}
