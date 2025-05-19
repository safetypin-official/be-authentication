package com.safetypin.authentication.model;

import com.safetypin.authentication.dto.UserResponse;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

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
    private String profileBanner; // used for jwt

    public UserResponse generateUserResponse() {
        return UserResponse.builder()
                .email(email)
                .id(id)
                .provider(provider)
                .birthdate(birthdate)
                .role(role)
                .name(name)
                .isVerified(this.isVerified()) // Explicitly call the getter
                .profilePicture(profilePicture)
                .profileBanner(profileBanner)
                .build();
    }
}
