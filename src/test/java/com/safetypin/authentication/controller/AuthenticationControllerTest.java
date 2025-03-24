package com.safetypin.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetypin.authentication.dto.*;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.model.RefreshToken;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.AuthenticationService;
import com.safetypin.authentication.service.GoogleAuthService;
import com.safetypin.authentication.service.JwtService;
import com.safetypin.authentication.service.RefreshTokenService;
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
    private GoogleAuthService googleAuthService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthenticationService authenticationService() {
            return Mockito.mock(AuthenticationService.class);
        }

        @Bean
        public GoogleAuthService googleAuthService() {
            return Mockito.mock(GoogleAuthService.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
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
        user.setRole(Role.REGISTERED_USER);
        user.setBirthdate(request.getBirthdate());
        user.setProvider("EMAIL");

        UUID id = UUID.randomUUID();
        user.setId(id);

        String accessToken = jwtService.generateToken(user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        AuthToken returnToken = new AuthToken(accessToken, refreshToken.getToken());

        Mockito.when(authenticationService.registerUser(any(RegistrationRequest.class))).thenReturn(returnToken);

        mockMvc.perform(post("/api/auth/register-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(accessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken.getToken()));
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

        UUID id = UUID.randomUUID();
        user.setId(id);


        String accessToken = jwtService.generateToken(user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        AuthToken returnToken = new AuthToken(accessToken, refreshToken.getToken());

        Mockito.when(authenticationService.loginUser("email@example.com", "password")).thenReturn(returnToken);

        mockMvc.perform(post("/api/auth/login-email")
                        .param("email", "email@example.com")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(accessToken))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken.getToken()));
    }

    @Test
    void testLoginEmail_InvalidCredentials() throws Exception {
        // Prepare test data
        String email = "email@example.com";
        String password = "invalidPassword";

        // Mock the service method to throw InvalidCredentialsException
        Mockito.when(authenticationService.loginUser(email, password))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        // Perform the test
        mockMvc.perform(post("/api/auth/login-email")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.data").doesNotExist()); // No data expected in case of error
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
    void testRegisterEmail_UserAlreadyExistsException() throws Exception {
        // Prepare registration request
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password");
        request.setName("Test User");
        request.setBirthdate(LocalDate.now().minusYears(20));

        // Mock the service to throw UserAlreadyExistsException
        String errorMessage = "User with email already exists";
        Mockito.when(authenticationService.registerUser(Mockito.any(RegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException(errorMessage));

        // Perform the POST request to /register-email endpoint
        mockMvc.perform(post("/api/auth/register-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void testDashboard() throws Exception {
        mockMvc.perform(get("/api/auth/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));
    }

    @Test
    void testAuthenticateGoogle_Success() throws Exception {
        // Prepare test data
        GoogleAuthDTO googleAuthData = new GoogleAuthDTO();
        googleAuthData.setIdToken("validGoogleToken");
        googleAuthData.setServerAuthCode("validServerAuthCode");

        // Generate a mock JWT token
        String mockJwt = "mockJwtToken123";
        String refreshToken = "test-refreshToken123";
        AuthToken returnToken = new AuthToken(mockJwt, refreshToken);

        // Mock the service method to return the JWT token
        Mockito.when(googleAuthService.authenticate(any(GoogleAuthDTO.class)))
                .thenReturn(returnToken);

        // Perform the test
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleAuthData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.accessToken").value(mockJwt))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));
    }

    @Test
    void testAuthenticateGoogle_MissingIdToken() throws Exception {
        // Prepare test data with missing idToken
        GoogleAuthDTO googleAuthData = new GoogleAuthDTO();
        googleAuthData.setServerAuthCode("validServerAuthCode");

        // Perform the test
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleAuthData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAuthenticateGoogle_MissingServerAuthCode() throws Exception {
        // Prepare test data with missing serverAuthCode
        GoogleAuthDTO googleAuthData = new GoogleAuthDTO();
        googleAuthData.setIdToken("validGoogleToken");

        // Perform the test
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleAuthData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAuthenticateGoogle_UserAlreadyExists() throws Exception {
        // Prepare test data
        GoogleAuthDTO googleAuthData = new GoogleAuthDTO();
        googleAuthData.setIdToken("existingUserToken");
        googleAuthData.setServerAuthCode("existingUserAuthCode");

        // Simulate UserAlreadyExistsException
        String errorMessage = "User with this email already exists";
        Mockito.when(googleAuthService.authenticate(any(GoogleAuthDTO.class)))
                .thenThrow(new UserAlreadyExistsException(errorMessage));

        // Perform the test
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleAuthData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void testAuthenticateGoogle_GeneralException() throws Exception {
        // Prepare test data
        GoogleAuthDTO googleAuthData = new GoogleAuthDTO();
        googleAuthData.setIdToken("invalidToken");
        googleAuthData.setServerAuthCode("invalidAuthCode");

        // Simulate a general exception
        String errorMessage = "Authentication failed: Invalid token";
        Mockito.when(googleAuthService.authenticate(any(GoogleAuthDTO.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // Perform the test
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleAuthData)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testAuthenticateGoogle_EmptyInput() throws Exception {
        // Perform test with empty input
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVerifyJwtToken() throws Exception {
        // Prepare test data
        String validToken = "valid.jwt.token";

        // Create a mock UserResponse
        UserResponse userResponse = Mockito.mock(UserResponse.class);
        UUID userId = UUID.randomUUID();

        // Set up basic behavior for the mock
        Mockito.when(userResponse.getId()).thenReturn(userId);
        Mockito.when(userResponse.getEmail()).thenReturn("test@example.com");
        Mockito.when(userResponse.getName()).thenReturn("Test User");

        // Mock the service method to return the mocked user response
        Mockito.when(jwtService.getUserFromJwtToken(validToken)).thenReturn(userResponse);

        // Perform the test
        mockMvc.perform(post("/api/auth/verify-jwt")
                        .param("token", validToken))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @Test
    void testVerifyJwtToken_InvalidToken() throws Exception {
        // Prepare test data
        String invalidToken = "invalid.jwt.token";

        // Mock the service method to throw InvalidCredentialsException
        Mockito.when(jwtService.getUserFromJwtToken(invalidToken))
                .thenThrow(new InvalidCredentialsException("Invalid token"));

        // Perform the test
        mockMvc.perform(post("/api/auth/verify-jwt")
                        .param("token", invalidToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid token"))
                .andExpect(jsonPath("$.data").doesNotExist()); // No data expected in case of error
    }

}
