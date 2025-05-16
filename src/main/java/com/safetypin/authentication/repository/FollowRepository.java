package com.safetypin.authentication.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.safetypin.authentication.model.Follow;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {
    List<Follow> findByFollowerId(UUID followerId);

    List<Follow> findByFollowingId(UUID followingId);

    Follow findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    void deleteByFollowerId(UUID followerId);

    void deleteByFollowingId(UUID followingId);

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    long countByFollowerId(UUID followerId); // Count following

    long countByFollowingId(UUID followingId); // Count followers

    // Find followers for a user within a specified time period
    List<Follow> findByFollowingIdAndCreatedAtAfterOrderByCreatedAtDesc(UUID followingId, LocalDateTime since);
}