package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.PasswordResetRequest;
import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.dto.SocialLoginRequest;
import com.safetypin.authentication.exception.InvalidCredentialsException;
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
    public User registerEmail(@Valid @RequestBody RegistrationRequest request) {
        return authenticationService.registerUser(request);
    }

    // Endpoint for social registration/login
    @PostMapping("/register-social")
    public User registerSocial(@Valid @RequestBody SocialLoginRequest request) {
        return authenticationService.socialLogin(request);
    }

    // OTP verification endpoint
    @PostMapping("/verify-otp")
    public String verifyOTP(@RequestParam String email, @RequestParam String otp) {
        boolean verified = authenticationService.verifyOTP(email, otp);
        return verified ? "User verified successfully" : "OTP verification failed";
    }



    // Endpoint for email login
    @PostMapping("/login-email")
    public ResponseEntity<?> loginEmail(@RequestParam String email, @RequestParam String password) {
        try {
            return ResponseEntity.ok(authenticationService.loginUser(email, password));
        } catch (InvalidCredentialsException e){
            //TODO
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }

    }

    // Endpoint for social login
    @PostMapping("/login-social")
    public User loginSocial(@RequestParam String email) {
        return authenticationService.loginSocial(email);
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
