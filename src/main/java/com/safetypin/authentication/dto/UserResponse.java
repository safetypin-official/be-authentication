package com.safetypin.authentication.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class UserResponse {
    private UUID id;

    private String email;

    private String name;

    private boolean isVerified;

    private String role;

    private LocalDate birthdate;

    private String provider;  // "EMAIL", "GOOGLE", "APPLE"

    private String profilePicture;

    private String profileBanner;
}
