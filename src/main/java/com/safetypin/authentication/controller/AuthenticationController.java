package com.safetypin.authentication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.safetypin.authentication.dto.ApiResponse;
import com.safetypin.authentication.dto.AuthToken;
import com.safetypin.authentication.dto.GoogleAuthDTO;
import com.safetypin.authentication.dto.LoginRequest;
import com.safetypin.authentication.dto.OTPRequest;
import com.safetypin.authentication.dto.PasswordResetRequest;
import com.safetypin.authentication.dto.PasswordResetWithOTPRequest;
import com.safetypin.authentication.dto.RegistrationRequest;
import com.safetypin.authentication.dto.ResetTokenResponse;
import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.dto.VerifyResetOTPRequest;
import com.safetypin.authentication.exception.InvalidCredentialsException;
import com.safetypin.authentication.exception.PendingVerificationException;
import com.safetypin.authentication.exception.UserAlreadyExistsException;
import com.safetypin.authentication.service.AuthenticationService;
import com.safetypin.authentication.service.GoogleAuthService;
import com.safetypin.authentication.service.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, GoogleAuthService googleAuthService,
            JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
    }

    // Endpoint for email registration
    @PostMapping("/register-email")
    public ResponseEntity<ApiResponse<AuthToken>> registerEmail(@Valid @RequestBody RegistrationRequest request) {
        try {
            AuthToken tokens = authenticationService.registerUser(request);
            // This part is reached only for successful *new* registration
            return ResponseEntity.ok().body(ApiResponse.success("OK", tokens));
        } catch (PendingVerificationException e) {
            // Handle case where user exists but is unverified (EMAIL provider)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException | UserAlreadyExistsException e) {
            // Handle other registration errors (underage, already verified, social provider)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // OTP verification endpoint
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOTP(@RequestBody OTPRequest otpRequest) {
        boolean verified = authenticationService.verifyOTP(otpRequest.getEmail(), otpRequest.getOtp());
        if (verified) {
            return ResponseEntity.ok().body(ApiResponse.success("User verified successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("OTP verification failed"));
        }
    }

    // Endpoint for email login
    @PostMapping("/login-email")
    public ResponseEntity<ApiResponse<AuthToken>> loginEmail(@RequestBody LoginRequest loginRequest) {
        try {
            AuthToken tokens = authenticationService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(ApiResponse.success("OK", tokens));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthToken>> authenticateGoogle(@Valid @RequestBody GoogleAuthDTO googleAuthData) {
        try {
            AuthToken tokens = googleAuthService.authenticate(googleAuthData);
            return ResponseEntity.ok(ApiResponse.success("OK", tokens));
        } catch (UserAlreadyExistsException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Endpoint for forgot password (only for email users)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            authenticationService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(
                    "Password reset OTP has been sent to your email", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Endpoint to verify OTP for password reset
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<ResetTokenResponse>> verifyResetOTP(@Valid @RequestBody VerifyResetOTPRequest request) {
        try {
            String resetToken = authenticationService.verifyPasswordResetOTP(request.getEmail(), request.getOtp());
            if (resetToken != null) {
                ResetTokenResponse tokenResponse = new ResetTokenResponse(resetToken);
                return ResponseEntity.ok(ApiResponse.success(
                        "OTP verified successfully. Reset token valid for 3 minutes.", tokenResponse));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid OTP"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Endpoint to reset password with reset token
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetWithOTPRequest request) {
        try {
            authenticationService.resetPassword(request.getEmail(), request.getNewPassword(), request.getResetToken());
            return ResponseEntity.ok(ApiResponse.success(
                    "Password has been reset successfully", null));
        } catch (InvalidCredentialsException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-jwt")
    public ResponseEntity<ApiResponse<UserResponse>> verifyJwtToken(@RequestParam String token) {
        try {
            UserResponse userResponse = jwtService.getUserFromJwtToken(token);
            return ResponseEntity.ok(ApiResponse.success("OK", userResponse));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthToken>> renewRefreshToken(@RequestParam String token) {
        try {
            AuthToken renewedTokens = authenticationService.renewRefreshToken(token);
            return ResponseEntity.ok(ApiResponse.success("OK", renewedTokens));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // On successful login, return an empty map as a placeholder for future reports
    @GetMapping("/dashboard")
    public String dashboard() {
        return "{}";
    }
}
