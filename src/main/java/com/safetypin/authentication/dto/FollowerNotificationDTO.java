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
public class FollowerNotificationDTO {
    private UUID userId;
    private String name;
    private String profilePicture;
    private LocalDateTime followedAt;
    private long daysAgo;
}