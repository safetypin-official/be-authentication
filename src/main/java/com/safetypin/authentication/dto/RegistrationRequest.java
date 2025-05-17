package com.safetypin.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class RegistrationRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotNull
    private LocalDate birthdate;

    // Getters and setters
    public void setEmail(String email) {
        this.email = (email != null) ? email.trim() : null;
    }

    public void setName(String name) {
        this.name = (name != null) ? name.trim() : null;
    }

}
