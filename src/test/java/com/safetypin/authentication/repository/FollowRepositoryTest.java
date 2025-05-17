package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.Follow;
import com.safetypin.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FollowRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FollowRepository followRepository;

    private User follower;
    private User followee1;
    private User followee2;
    private Follow follow1;
    private Follow follow2;

    @BeforeEach
    void setUp() {
        follower = new User();
        follower.setName("Follower User");
        follower.setEmail("follower@example.com");
        entityManager.persist(follower);

        followee1 = new User();
        followee1.setName("Followee 1");
        followee1.setEmail("followee1@example.com");
        entityManager.persist(followee1);

        followee2 = new User();
        followee2.setName("Followee 2");
        followee2.setEmail("followee2@example.com");
        entityManager.persist(followee2);

        follow1 = new Follow();
        follow1.setFollowerId(follower.getId());
        follow1.setFollowingId(followee1.getId());
        follow1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(follow1);

        follow2 = new Follow();
        follow2.setFollowerId(follower.getId());
        follow2.setFollowingId(followee2.getId());
        follow2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(follow2);

        // Add a follower to followee1
        Follow followBackToFollowee1 = new Follow();
        followBackToFollowee1.setFollowerId(followee2.getId());
        followBackToFollowee1.setFollowingId(followee1.getId());
        followBackToFollowee1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(followBackToFollowee1);

        entityManager.flush();
    }

    @Test
    void findByFollowerId_ReturnsFollowingRelationships() {
        // Act
        List<Follow> following = followRepository.findByFollowerId(follower.getId());

        // Assert
        assertEquals(2, following.size());
        assertTrue(following.stream().anyMatch(f -> f.getFollowingId().equals(followee1.getId())));
        assertTrue(following.stream().anyMatch(f -> f.getFollowingId().equals(followee2.getId())));
    }

    @Test
    void findByFollowingId_ReturnsFollowerRelationships() {
        // Act
        List<Follow> followers = followRepository.findByFollowingId(followee1.getId());

        // Assert
        assertEquals(2, followers.size());
        assertTrue(followers.stream().anyMatch(f -> f.getFollowerId().equals(follower.getId())));
        assertTrue(followers.stream().anyMatch(f -> f.getFollowerId().equals(followee2.getId())));
    }

    @Test
    void findByFollowerIdAndFollowingId_ReturnsRelationship() {
        // Act
        Follow result = followRepository.findByFollowerIdAndFollowingId(
                follower.getId(), followee1.getId());

        // Assert
        assertNotNull(result);
        assertEquals(follower.getId(), result.getFollowerId());
        assertEquals(followee1.getId(), result.getFollowingId());
    }

    @Test
    void existsByFollowerIdAndFollowingId_WhenExists_ReturnsTrue() {
        // Act
        boolean exists = followRepository.existsByFollowerIdAndFollowingId(
                follower.getId(), followee1.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByFollowerIdAndFollowingId_WhenDoesNotExist_ReturnsFalse() {
        // Act
        boolean exists = followRepository.existsByFollowerIdAndFollowingId(
                followee1.getId(), follower.getId());

        // Assert
        assertFalse(exists);
    }

    @Test
    void countByFollowerId_ReturnsCorrectCount() {
        // Act
        long count = followRepository.countByFollowerId(follower.getId());

        // Assert
        assertEquals(2, count);
    }

    @Test
    void countByFollowingId_ReturnsCorrectCount() {
        // Act
        long count = followRepository.countByFollowingId(followee1.getId());

        // Assert
        assertEquals(2, count);
    }

    @Test
    void deleteByFollowerIdAndFollowingId_RemovesRelationship() {
        // Act
        followRepository.deleteByFollowerIdAndFollowingId(follower.getId(), followee1.getId());
        entityManager.flush();

        // Assert
        assertFalse(followRepository.existsByFollowerIdAndFollowingId(follower.getId(), followee1.getId()));

        // Verify other relationships are still intact
        assertTrue(followRepository.existsByFollowerIdAndFollowingId(follower.getId(), followee2.getId()));
        assertTrue(followRepository.existsByFollowerIdAndFollowingId(followee2.getId(), followee1.getId()));
    }
}