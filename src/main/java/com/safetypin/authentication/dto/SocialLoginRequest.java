package com.safetypin.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class SocialLoginRequest {

    @NotBlank
    private String provider; // "GOOGLE" or "APPLE"

    @NotBlank
    private String socialToken; // Token from the social provider

    // Simulated fields as if retrieved from the provider
    @NotBlank
    private String email;

    @NotBlank
    private String name;

    @NotNull
    private LocalDate birthdate;

    @NotBlank
    private String socialId; // ID provided by the social provider

    // Getters and setters

}
