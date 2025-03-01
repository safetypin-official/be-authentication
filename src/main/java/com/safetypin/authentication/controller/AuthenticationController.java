package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.AuthResponse;
import com.safetypin.authentication.dto.PasswordResetRequest;
import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.dto.SocialLoginRequest;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
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
        User user;
        try {
            user = authenticationService.registerUser(request);
        } catch (IllegalArgumentException | UserAlreadyExistsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok().body(new AuthResponse(true, "OK", user));
    }

    // Endpoint for social registration/login
    @PostMapping("/register-social")
    public ResponseEntity<AuthResponse> registerSocial(@Valid @RequestBody SocialLoginRequest request) {
        User user;
        try {
            user = authenticationService.socialLogin(request);
        } catch (IllegalArgumentException | UserAlreadyExistsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok().body(new AuthResponse(true, "OK", user));
    }

    // OTP verification endpoint
    @PostMapping("/verify-otp")
    public String verifyOTP(@RequestParam String email, @RequestParam String otp) {
        boolean verified = authenticationService.verifyOTP(email, otp);
        return verified ? "User verified successfully" : "OTP verification failed";
    }



    // Endpoint for email login
    @PostMapping("/login-email")
    public ResponseEntity<Object> loginEmail(@RequestParam String email, @RequestParam String password) {
        try {
            return ResponseEntity.ok(authenticationService.loginUser(email, password));
        } catch (InvalidCredentialsException e){
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    // Endpoint for social login
    @PostMapping("/login-social")
    public ResponseEntity<Object> loginSocial(@RequestParam String email) {
        try {
            return ResponseEntity.ok(authenticationService.loginSocial(email));
        } catch (InvalidCredentialsException e){
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




    // Endpoint simulating a content post that requires a verified account
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
