package com.safetypin.authentication.dto;

import java.util.UUID;

import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private UUID id;
    private Role role;
    private boolean isVerified;
    private String name;
    private String instagram;
    private String twitter;
    private String line;
    private String tiktok;
    private String discord;
    private String profilePicture;
    private String profileBanner;
    private long followersCount;
    private long followingCount;
    private boolean isFollowing;

    public static ProfileResponse fromUser(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .instagram(user.getInstagram())
                .twitter(user.getTwitter())
                .line(user.getLine())
                .tiktok(user.getTiktok())
                .discord(user.getDiscord())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .profileBanner(user.getProfileBanner())
                .build();
    } // Maybe better to set followings as a separate DTO

    public static ProfileResponse fromUserAndFollowStatus(
            User user, long followersCount, long followingCount, boolean isFollowing) {
        return ProfileResponse.builder()
                .id(user.getId())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .instagram(user.getInstagram())
                .twitter(user.getTwitter())
                .line(user.getLine())
                .tiktok(user.getTiktok())
                .discord(user.getDiscord())
                .name(user.getName())
                .profilePicture(user.getProfilePicture())
                .profileBanner(user.getProfileBanner())
                .followersCount(followersCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();
    }
}
