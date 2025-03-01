package com.safetypin.authentication.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @Setter @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter @Getter
    @Column(nullable = false, unique = true)
    private String email;

    // May be null for social login users
    @Setter @Getter
    @Column(nullable = false)
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

    @Setter @Getter
    private String socialId;  // For social login users


    // Getters and setters

    public boolean isVerified() {
        return isVerified;
    }
    public void setVerified(boolean verified) {
        isVerified = verified;
    }

}
