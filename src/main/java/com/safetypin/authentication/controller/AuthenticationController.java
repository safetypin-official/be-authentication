package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.*;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.service.AuthenticationService;
import com.safetypin.authentication.service.GoogleAuthService;
import com.safetypin.authentication.service.JwtService;
import jakarta.validation.Valid;
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

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, GoogleAuthService googleAuthService, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
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

    // OTP verification endpoint
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOTP(@RequestBody OTPRequest otpRequest) {
        boolean verified = authenticationService.verifyOTP(otpRequest.getEmail(), otpRequest.getOtp());
        if (verified) {
            return ResponseEntity.ok().body(new AuthResponse(true, "User verified successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(false, "OTP verification failed", null));
        }

    }


    // Endpoint for email login
    @PostMapping("/login-email")
    public ResponseEntity<AuthResponse> loginEmail(@RequestBody LoginRequest loginRequest) {
        try {
            String jwt = authenticationService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(new AuthResponse(true, "OK", new Token(jwt)));
        } catch (InvalidCredentialsException e) {
            AuthResponse response = new AuthResponse(false, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> authenticateGoogle(@Valid @RequestBody GoogleAuthDTO googleAuthData) {
        try {
            String jwt = googleAuthService.authenticate(googleAuthData);
            return ResponseEntity.ok(new AuthResponse(true, "OK", new Token(jwt)));
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
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            authenticationService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(new AuthResponse(true,
                    "Password reset OTP has been sent to your email", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, e.getMessage(), null));
        }
    }

    // Endpoint to verify OTP for password reset
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<AuthResponse> verifyResetOTP(@Valid @RequestBody VerifyResetOTPRequest request) {
        try {
            String resetToken = authenticationService.verifyPasswordResetOTP(request.getEmail(), request.getOtp());
            if (resetToken != null) {
                ResetTokenResponse tokenResponse = new ResetTokenResponse(resetToken);
                return ResponseEntity.ok(new AuthResponse(true,
                        "OTP verified successfully. Reset token valid for 3 minutes.", tokenResponse));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse(false, "Invalid OTP", null));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, e.getMessage(), null));
        }
    }

    // Endpoint to reset password with reset token
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody PasswordResetWithOTPRequest request) {
        try {
            authenticationService.resetPassword(request.getEmail(), request.getNewPassword(), request.getResetToken());
            return ResponseEntity.ok(new AuthResponse(true,
                    "Password has been reset successfully", null));
        } catch (InvalidCredentialsException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, e.getMessage(), null));
        }
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

    // On successful login, return an empty map as a placeholder for future reports
    @GetMapping("/dashboard")
    public String dashboard() {
        return "{}";
    }

}
