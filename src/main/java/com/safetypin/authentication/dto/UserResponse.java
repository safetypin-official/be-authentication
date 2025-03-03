package com.safetypin.authentication.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@Setter
@Builder
public class UserResponse {
    private Long id;

    private String email;

    private String name;

    private boolean isVerified;

    private String role;

    private LocalDate birthdate;

    private String provider;  // "EMAIL", "GOOGLE", "APPLE"


}
