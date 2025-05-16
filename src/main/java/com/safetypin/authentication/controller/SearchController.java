package com.safetypin.authentication.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.FollowService;
import com.safetypin.authentication.service.UserService;

@RestController
@RequestMapping("/api/users")
public class SearchController {
    private final UserService userService;
    private final FollowService followService;

    public SearchController(UserService userService, FollowService followService) {
        this.userService = userService;
        this.followService = followService;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsersByName(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<User> users;
        if (query == null || query.trim().isEmpty()) {
            users = userService.findAllUsers(); // Fetch all users if no query is provided
        } else {
            users = userService.findUsersByNameContaining(query.trim());
        }

        // Convert users to UserResponse DTOs and set followers count for each
        List<UserResponse> userResponses = users.stream()
                .map(user -> {
                    UserResponse response = user.generateUserResponse();
                    // Set followers count for each user
                    response.setFollowersCount(followService.getFollowersCount(user.getId()));
                    return response;
                })
                .sorted(Comparator.comparing(UserResponse::getFollowersCount).reversed())
                .toList(); // Sorted by followers count in descending order

        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), userResponses.size());

        List<UserResponse> userResponsesContent = start >= userResponses.size() ? Collections.emptyList()
                : userResponses.subList(start, end);

        return ResponseEntity.ok(
                new PageImpl<>(userResponsesContent, pageable, userResponses.size()));
    }
}