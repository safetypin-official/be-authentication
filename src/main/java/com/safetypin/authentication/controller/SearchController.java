package com.safetypin.authentication.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
// Import PostMapping
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.safetypin.authentication.dto.UserResponse; // Add missing import
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.UserService;

@RestController
@RequestMapping("/api/users")
public class SearchController {
    private final UserService userService;

    public SearchController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsersByName(@RequestParam(required = false) String query) {
        List<User> users;
        if (query == null || query.trim().isEmpty()) {
            users = userService.findAllUsers(); // Fetch all users if no query is provided
        } else {
            users = userService.findUsersByNameContaining(query.trim());
        }
        List<UserResponse> userResponses = users.stream()
                .map(User::generateUserResponse)
                .toList(); // Use toList()
        return ResponseEntity.ok(userResponses);
    }
}