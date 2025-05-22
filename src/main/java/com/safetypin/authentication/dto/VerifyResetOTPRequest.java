package com.safetypin.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VerifyResetOTPRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "OTP is required")
    private String otp;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = (email != null) ? email.trim() : null;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = (otp != null) ? otp.trim() : null;
    }
}
