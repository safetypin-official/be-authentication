package com.safetypin.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordResetWithOTPRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;

    @NotBlank(message = "Reset token is required")
    private String resetToken;

    public void setEmail(String email) {
        this.email = (email != null) ? email.trim() : null;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = (resetToken != null) ? resetToken.trim() : null;
    }
}
