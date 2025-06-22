package com.safetypin.authentication.service;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.safetypin.authentication.exception.ResourceNotFoundException;
import com.safetypin.authentication.exception.UnauthorizedAccessException;
import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.FollowRepository;
import com.safetypin.authentication.repository.ProfileViewRepository;
import com.safetypin.authentication.repository.RefreshTokenRepository;
import com.safetypin.authentication.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserAdminService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProfileViewRepository profileViewRepository;
    private final FollowRepository followRepository;
    private final RestTemplate restTemplate;
    private final JwtService jwtService; // Added JwtService
    @Value("${be-post}")
    private String postServiceUrl;

    @Autowired
    public UserAdminService(UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            ProfileViewRepository profileViewRepository,
            FollowRepository followRepository,
            RestTemplate restTemplate,
            JwtService jwtService) { // Added JwtService to constructor
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.profileViewRepository = profileViewRepository;
        this.followRepository = followRepository;
        this.restTemplate = restTemplate;
        this.jwtService = jwtService; // Initialize JwtService
    }

    /**
     * Delete a user account and all associated data
     * This operation can only be performed by a user with MODERATOR role
     *
     * @param moderatorId  the ID of the moderator attempting the deletion
     * @param targetUserId the ID of the user to delete
     * @throws ResourceNotFoundException   if user is not found
     * @throws UnauthorizedAccessException if the caller is not a moderator
     */
    @Transactional
    public void deleteUser(UUID moderatorId, UUID targetUserId) {
        // Check if moderator exists and has MODERATOR role
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Moderator not found with id " + moderatorId));

        if (moderator.getRole() != Role.MODERATOR) {
            throw new UnauthorizedAccessException("Only moderators can delete user accounts");
        }

        // Check if target user exists
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + targetUserId));

        // Delete all refresh tokens associated with the user
        refreshTokenRepository.deleteByUserId(targetUserId);

        // Delete all profile views where user is the viewer or the viewed user
        profileViewRepository.deleteByViewerId(targetUserId);
        profileViewRepository.deleteByUserId(targetUserId);

        // Delete all follows where user is the follower or the following user
        followRepository.deleteByFollowerId(targetUserId);
        followRepository.deleteByFollowingId(targetUserId);
        // Delete the user from the database
        userRepository.delete(targetUser);

        // Notify post microservice to delete all posts by this user
        try {
            String url = postServiceUrl + "/posts/delete/" + targetUserId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            // Add JWT token to Authorization header
            String token = jwtService.generateToken(moderatorId);
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.info("Successfully notified post service to delete posts for user: {}", targetUserId);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            log.error("Failed to notify post service to delete posts for user: {}", targetUserId, e);
        }
    }
}
