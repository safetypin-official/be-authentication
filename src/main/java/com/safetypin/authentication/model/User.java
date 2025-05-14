package com.safetypin.authentication.model;

import java.time.LocalDate;
import java.util.UUID;

import com.safetypin.authentication.dto.UserResponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Data
public class User {
    @Id
    @Setter
    @Getter
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @Getter
    @Column(nullable = false, unique = true)
    private String email;

    // May be null for social login users
    @Setter
    @Getter
    private String password;

    @Setter
    @Getter
    @Column(nullable = false, length = 100)
    private String name;

    @Getter // Explicitly add getter for boolean field for Jackson/Lombok interaction
    @Column(nullable = false)
    private boolean isVerified = false;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Role role;

    // New fields
    @Setter
    @Getter
    private LocalDate birthdate;

    @Setter
    @Getter
    private String provider; // "EMAIL", "GOOGLE", "APPLE"

    @Setter
    @Getter
    private String instagram;

    @Setter
    @Getter
    private String twitter;

    @Setter
    @Getter
    private String line;

    @Setter
    @Getter
    private String tiktok;

    @Setter
    @Getter
    private String discord;

    @Setter
    @Getter
    private String profilePicture;

    @Setter
    @Getter
    private String profileBanner;

    // used for jwt
    public UserResponse generateUserResponse() {
        return UserResponse.builder()
                .email(email)
                .id(id)
                .provider(provider)
                .birthdate(birthdate)
                .role(role != null ? role.name() : null)
                .name(name)
                .isVerified(this.isVerified()) // Explicitly call the getter
                .profilePicture(profilePicture)
                .profileBanner(profileBanner)
                .build();
    }
}
