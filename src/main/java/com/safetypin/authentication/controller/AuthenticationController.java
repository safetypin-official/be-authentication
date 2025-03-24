package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.*;
import com.safetypin.authentication.exception.ApiException;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.RefreshToken;
import com.safetypin.authentication.service.AuthenticationService;
import com.safetypin.authentication.service.RefreshTokenService;
import jakarta.validation.Valid;
import com.safetypin.authentication.service.GoogleAuthService;
import com.safetypin.authentication.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, GoogleAuthService googleAuthService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.authenticationService = authenticationService;
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }


    // Endpoint for email registration
    @PostMapping("/register-email")
    public ResponseEntity<AuthResponse> registerEmail(@Valid @RequestBody RegistrationRequest request) {
        try {
            AuthToken tokens = authenticationService.registerUser(request);
            return ResponseEntity.ok().body(new AuthResponse(true, "OK", tokens));
        } catch (IllegalArgumentException | UserAlreadyExistsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    // OTP verification endpoint
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOTP(@RequestParam String email, @RequestParam String otp) {
        boolean verified = authenticationService.verifyOTP(email, otp);
        if (verified) {
            return ResponseEntity.ok().body(new AuthResponse(true, "User verified successfully", null));
        } else {
            return ResponseEntity.ok().body(new AuthResponse(false, "OTP verification failed", null));
        }

    }


    // Endpoint for email login
    @PostMapping("/login-email")
    public ResponseEntity<AuthResponse> loginEmail(@RequestParam String email, @RequestParam String password) {
        try {
            AuthToken tokens = authenticationService.loginUser(email, password);
            return ResponseEntity.ok(new AuthResponse(true, "OK", tokens));
        } catch (InvalidCredentialsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> authenticateGoogle(@Valid @RequestBody GoogleAuthDTO googleAuthData) {
        try {
            AuthToken tokens = googleAuthService.authenticate(googleAuthData);
            return ResponseEntity.ok(new AuthResponse(true, "OK", tokens));
        } catch (UserAlreadyExistsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // Endpoint for forgot password (only for email users)
    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        authenticationService.forgotPassword(request.getEmail());
        return "Password reset instructions have been sent to your email (simulated)";
    }

    @PostMapping("/verify-jwt")
    public ResponseEntity<AuthResponse> verifyJwtToken(@RequestParam String token) {
        try {
            UserResponse userResponse = jwtService.getUserFromJwtToken(token);
            return ResponseEntity.ok(new AuthResponse(true, "OK", userResponse));
        } catch (InvalidCredentialsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> renewRefreshToken(@RequestParam String token) {
        try {
            RefreshToken renewedRefreshToken = refreshTokenService.renewRefreshToken(token);
            return ResponseEntity.ok(new AuthResponse(true, "OK", renewedRefreshToken.getToken()));
        } catch (ApiException|InvalidCredentialsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    // On successful login, return an empty map as a placeholder for future reports
    @GetMapping("/dashboard")
    public String dashboard() {
        return "{}";
    }

}
