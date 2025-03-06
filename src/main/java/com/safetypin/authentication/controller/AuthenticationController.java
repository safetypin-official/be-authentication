package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.*;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    // Endpoint for email registration
    @PostMapping("/register-email")
    public ResponseEntity<AuthResponse> registerEmail(@Valid @RequestBody RegistrationRequest request) {
        try {
            String jwt = authenticationService.registerUser(request);
            return ResponseEntity.ok().body(new AuthResponse(true, "OK", new Token(jwt)));
        } catch (IllegalArgumentException | UserAlreadyExistsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    // Endpoint for social registration/login
    @PostMapping("/register-social")
    public ResponseEntity<AuthResponse> registerSocial(@Valid @RequestBody SocialLoginRequest request) {
        try {
            String jwt = authenticationService.socialLogin(request);
            return ResponseEntity.ok().body(new AuthResponse(true, "OK", new Token(jwt)));
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
            String jwt = authenticationService.loginUser(email, password);
            return ResponseEntity.ok(new AuthResponse(true, "OK", new Token(jwt)));
        } catch (InvalidCredentialsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    // Endpoint for social login (DEPRECATED, use regis-social instead)
    @PostMapping("/login-social")
    public ResponseEntity<AuthResponse> loginSocial(@RequestParam String email) {
        try {
            String jwt = authenticationService.loginSocial(email);
            return ResponseEntity.ok(new AuthResponse(true, "OK", new Token(jwt)));
        } catch (InvalidCredentialsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
            UserResponse userResponse = authenticationService.getUserFromJwtToken(token);
            return ResponseEntity.ok(new AuthResponse(true, "OK", userResponse));
        } catch (InvalidCredentialsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    // Endpoint simulating a content post that requires a verified account (DEPRECATED, use be-post instead)
    @PostMapping("/post")
    public String postContent(@RequestParam String email, @RequestParam String content) {
        return authenticationService.postContent(email, content);
    }

    // On successful login, return an empty map as a placeholder for future reports
    @GetMapping("/dashboard")
    public String dashboard() {
        return "{}";
    }

}
