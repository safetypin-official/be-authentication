package com.safetypin.authentication.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.safetypin.authentication.model.Role;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
// Yang disimpan di dalam JWT
public class UserResponse {
    private UUID id;

    private String email;

    private String name;

    private boolean isVerified;

    private Role role;

    private LocalDate birthdate;

    private String provider; // "EMAIL", "GOOGLE", "APPLE"

    private String profileBanner;

    private String profilePicture;

    private long followersCount;
}
