package com.safetypin.authentication.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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