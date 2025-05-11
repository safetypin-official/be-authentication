package com.safetypin.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowStats {
    private long followersCount;
    private long followingCount;
    private boolean isFollowing;
}