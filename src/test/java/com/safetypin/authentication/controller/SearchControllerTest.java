package com.safetypin.authentication.controller;

import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.safetypin.authentication.model.Role;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Load full app
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Keep the same instance for DB setup
@Transactional // Rollback after each test
class SearchControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository; // Use actual repository

    @BeforeAll
    void setUp() {
        userRepository.deleteAll(); // Clean DB before tests

        // Insert real users into the database with non-nullable fields
        User user1 = new User();
        user1.setName("John Doe");
        user1.setEmail("john.doe@example.com");
        user1.setRole(Role.REGISTERED_USER);

        User user2 = new User();
        user2.setName("John Richard");
        user2.setEmail("john.richard@example.com");
        user2.setRole(Role.REGISTERED_USER);

        User user3 = new User();
        user3.setName("Koala Kom");
        user3.setEmail("koala.kom@example.com");
        user3.setRole(Role.REGISTERED_USER);

        userRepository.saveAll(List.of(user1, user2, user3));
    }

    @Test
    void testSearchMultipleUsersByName() throws Exception {

        mockMvc.perform(get("/api/users/search")
                        .param("query", "john")) // Actual DB query
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2)) // Expect only 2 users with "John"
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("John Richard"));
    }

    @Test
    void testSearchAllUsersWhenNoQueryProvided() throws Exception {
        mockMvc.perform(get("/api/users/search")) // No query parameter
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3)) // Expect all 3 users
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("John Richard"))
                .andExpect(jsonPath("$[2].name").value("Koala Kom"));
    }

    @Test
    void testSearchAllUsersWhenEmptyQuery() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("query", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3)) // Expect all 3 users
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("John Richard"))
                .andExpect(jsonPath("$[2].name").value("Koala Kom"));
    }
}
