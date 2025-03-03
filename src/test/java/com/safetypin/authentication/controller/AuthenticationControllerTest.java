package com.safetypin.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetypin.authentication.dto.PasswordResetRequest;
import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.dto.SocialLoginRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import({AuthenticationControllerTest.TestConfig.class, AuthenticationControllerTest.TestSecurityConfig.class})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthenticationService authenticationService() {
            return Mockito.mock(AuthenticationService.class);
        }
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
            return http.build();
        }
    }


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
        user.setRole("USER");
        user.setBirthdate(request.getBirthdate());
        user.setProvider("EMAIL");

        user.setId(1L);
        Mockito.when(authenticationService.registerUser(any(RegistrationRequest.class))).thenReturn(user.generateUserResponse());

        mockMvc.perform(post("/api/auth/register-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.email").value("email@example.com"));
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
        user.setRole("USER");
        user.setBirthdate(request.getBirthdate());
        user.setProvider("GOOGLE");
        user.setSocialId("social123");
        user.setId(2L);
        Mockito.when(authenticationService.socialLogin(any(SocialLoginRequest.class))).thenReturn(user.generateUserResponse());

        mockMvc.perform(post("/api/auth/register-social")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2L))
                .andExpect(jsonPath("$.data.email").value("social@example.com"));
    }

    @Test
    void testLoginEmail() throws Exception {
        User user = new User();
        user.setEmail("email@example.com");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setVerified(true);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(20));
        user.setProvider("EMAIL");
        user.setSocialId(null);

        user.setId(1L);
        Mockito.when(authenticationService.loginUser("email@example.com", "password")).thenReturn(user.generateUserResponse());

        mockMvc.perform(post("/api/auth/login-email")
                        .param("email", "email@example.com")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("email@example.com"));
    }

    @Test
    void testLoginSocial() throws Exception {
        User user = new User();
        user.setEmail("social@example.com");
        user.setPassword(null);
        user.setName("Social User");
        user.setVerified(true);
        user.setRole("USER");
        user.setBirthdate(LocalDate.now().minusYears(25));
        user.setProvider("GOOGLE");
        user.setSocialId("social123");
        user.setId(2L);
        Mockito.when(authenticationService.loginSocial("social@example.com")).thenReturn(user.generateUserResponse());

        mockMvc.perform(post("/api/auth/login-social")
                        .param("email", "social@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.email").value("social@example.com"));
    }

    @Test
    void testVerifyOTP_Success() throws Exception {
        Mockito.when(authenticationService.verifyOTP("email@example.com", "123456")).thenReturn(true);

        mockMvc.perform(post("/api/auth/verify-otp")
                        .param("email", "email@example.com")
                        .param("otp", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("User verified successfully"));
    }

    @Test
    void testVerifyOTP_Failure() throws Exception {
        Mockito.when(authenticationService.verifyOTP("email@example.com", "000000")).thenReturn(false);

        mockMvc.perform(post("/api/auth/verify-otp")
                        .param("email", "email@example.com")
                        .param("otp", "000000"))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP verification failed"));
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
}
