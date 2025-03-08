package com.safetypin.authentication.model;

import com.safetypin.authentication.dto.UserResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    @Setter @Getter
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter @Getter
    @Column(nullable = false, unique = true)
    private String email;

    // May be null for social login users
    @Setter @Getter
    private String password;

    @Setter @Getter
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Setter @Getter
    private String role;

    // New fields
    @Setter @Getter
    private LocalDate birthdate;

    @Setter  @Getter
    private String provider;  // "EMAIL", "GOOGLE", "APPLE"

    // Getters and setters

    public boolean isVerified() {
        return isVerified;
    }
    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public UserResponse generateUserResponse(){
        return UserResponse.builder()
                .email(email)
                .id(id)
                .provider(provider)
                .birthdate(birthdate)
                .role(role)
                .name(name)
                .isVerified(isVerified)
                .build();
    }

}
