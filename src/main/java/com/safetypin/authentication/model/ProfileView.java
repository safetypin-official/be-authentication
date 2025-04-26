package com.safetypin.authentication.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "profile_view")
public class ProfileView {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "viewer_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User viewer;

    private LocalDateTime viewedAt;
}


