package com.safetypin.authentication.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Column(nullable = false, unique = true)
    private String email;

    // May be null for social login users
    @Setter
    @Getter
    @Column(nullable = false)
    private String password;

    @Setter
    @Getter
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Setter
    @Getter
    private String role;

    // New fields
    @Setter
    @Getter
    private LocalDate birthdate;
    @Setter
    @Getter
    private String provider;  // "EMAIL", "GOOGLE", "APPLE"
    @Setter
    @Getter
    private String socialId;  // For social login users

    public User() {}

    public User(String email, String password, String name, boolean isVerified, String role,
                LocalDate birthdate, String provider, String socialId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.isVerified = isVerified;
        this.role = role;
        this.birthdate = birthdate;
        this.provider = provider;
        this.socialId = socialId;
    }

    // Getters and setters

    public boolean isVerified() {
        return isVerified;
    }
    public void setVerified(boolean verified) {
        isVerified = verified;
    }

}
