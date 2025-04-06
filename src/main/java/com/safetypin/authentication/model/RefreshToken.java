package com.safetypin.authentication.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private Instant expiryTime;
}

