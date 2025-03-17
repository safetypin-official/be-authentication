package com.safetypin.authentication.controller;

import com.safetypin.authentication.dto.UserResponse;
import com.safetypin.authentication.model.User;
import com.safetypin.authentication.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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
            users = userService.findUsersByNameContaining(query);
        }
        List<UserResponse> userResponses = users.stream()
                .map(User::generateUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }
}