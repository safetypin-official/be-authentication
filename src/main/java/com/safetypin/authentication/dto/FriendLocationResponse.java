package com.safetypin.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendLocationResponse {
    private UUID id;
    private String name;
    private String profilePicture;
    private double latitude;
    private double longitude;
    private LocalDateTime updatedAt;
}