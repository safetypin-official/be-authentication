package com.safetypin.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordResetRequest {

    @NotBlank
    @Email
    private String email;

    // Getters and setters

}
