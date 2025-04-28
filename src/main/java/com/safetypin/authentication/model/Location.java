package com.safetypin.authentication.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private double latitude;
    
    @Column(nullable = false)
    private double longitude;
    
    private double accuracy;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }
}
