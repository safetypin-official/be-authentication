package com.safetypin.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void testFindById_WhenUserExists_ReturnUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User expectedUser = new User();
        expectedUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<User> result = userService.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userRepository).findById(userId);
    }

    @Test
    void testFindById_WhenUserDoesNotExist_ReturnEmptyOptional() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void testFindByEmail_WhenUserExists_ReturnUser() {
        // Arrange
        String email = "test@example.com";
        User expectedUser = new User();
        expectedUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(expectedUser);

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testFindByEmail_WhenUserDoesNotExist_ReturnEmptyOptional() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void testSave_ShouldCallRepositorySaveAndReturnUser() {
        // Arrange
        User userToSave = new User();
        userToSave.setEmail("test@example.com");

        when(userRepository.save(any(User.class))).thenReturn(userToSave);

        // Act
        User savedUser = userService.save(userToSave);

        // Assert
        assertNotNull(savedUser);
        assertEquals(userToSave, savedUser);
        verify(userRepository).save(userToSave);
    }

    @Test
    void testFindUsersByNameContaining_ShouldReturnMatchingUsers() {
        // Arrange
        String query = "test";
        User user1 = new User();
        user1.setName("Test User 1");
        User user2 = new User();
        user2.setName("Another Test User");
        List<User> expectedUsers = Arrays.asList(user1, user2);

        when(userRepository.findByNameContainingIgnoreCase(query)).thenReturn(expectedUsers);

        // Act
        List<User> result = userService.findUsersByNameContaining(query);

        // Assert
        assertEquals(expectedUsers, result);
        verify(userRepository).findByNameContainingIgnoreCase(query);
    }

    @Test
    void testFindAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setName("User 1");
        User user2 = new User();
        user2.setName("User 2");
        List<User> expectedUsers = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userService.findAllUsers();

        // Assert
        assertEquals(expectedUsers, result);
        verify(userRepository).findAll();
    }

    @Test
    void testConstructor_InitializesRepositoryCorrectly() {
        // Arrange & Act
        UserService service = new UserService(userRepository);

        // Assert
        assertNotNull(service);

        // Verify that the repository is correctly initialized by calling a method
        UUID randomId = UUID.randomUUID();
        service.findById(randomId);
        verify(userRepository).findById(randomId);
    }
}