package com.safetypin.authentication.service;

import com.safetypin.authentication.model.User;
import com.safetypin.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

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

    @Nested
    class FindAllByIdTests {
        private User user1, user2, user3;
        private UUID id1, id2, id3, nonExistentId;

        @BeforeEach
        void setUp() {
            id1 = UUID.randomUUID();
            id2 = UUID.randomUUID();
            id3 = UUID.randomUUID();
            nonExistentId = UUID.randomUUID();

            user1 = new User();
            user1.setId(id1);
            user1.setName("User 1");

            user2 = new User();
            user2.setId(id2);
            user2.setName("User 2");

            user3 = new User();
            user3.setId(id3);
            user3.setName("User 3");
        }

        @Test
        void findAllById_AllIdsExist_ReturnsAllUsers() {
            // Arrange
            List<UUID> ids = Arrays.asList(id1, id2, id3);
            List<User> expectedUsers = Arrays.asList(user1, user2, user3);
            when(userRepository.findAllById(ids)).thenReturn(expectedUsers);

            // Act
            List<User> result = userService.findAllById(ids);

            // Assert
            assertEquals(3, result.size());
            assertTrue(result.contains(user1));
            assertTrue(result.contains(user2));
            assertTrue(result.contains(user3));
            verify(userRepository, times(1)).findAllById(ids);
        }

        @Test
        void findAllById_SomeIdsExist_ReturnsFoundUsers() {
            // Arrange
            List<UUID> ids = Arrays.asList(id1, nonExistentId, id3);
            List<User> expectedUsers = Arrays.asList(user1, user3);
            when(userRepository.findAllById(ids)).thenReturn(expectedUsers);

            // Act
            List<User> result = userService.findAllById(ids);

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.contains(user1));
            assertTrue(result.contains(user3));
            assertFalse(result.contains(user2));
            verify(userRepository, times(1)).findAllById(ids);
        }

        @Test
        void findAllById_NoIdsExist_ReturnsEmptyList() {
            // Arrange
            List<UUID> ids = Arrays.asList(nonExistentId, UUID.randomUUID());
            when(userRepository.findAllById(ids)).thenReturn(Collections.emptyList());

            // Act
            List<User> result = userService.findAllById(ids);

            // Assert
            assertTrue(result.isEmpty());
            verify(userRepository, times(1)).findAllById(ids);
        }

        @Test
        void findAllById_EmptyIdList_ReturnsEmptyList() {
            // Arrange
            List<UUID> ids = Collections.emptyList();
            when(userRepository.findAllById(ids)).thenReturn(Collections.emptyList());

            // Act
            List<User> result = userService.findAllById(ids);

            // Assert
            assertTrue(result.isEmpty());
            verify(userRepository, times(1)).findAllById(ids);
        }
    }
}