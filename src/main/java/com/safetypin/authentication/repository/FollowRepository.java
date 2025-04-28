package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.Follow;
import com.safetypin.authentication.model.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    
    // Find users who the userId is following
    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :userId")
    List<UUID> findFollowingIds(@Param("userId") UUID userId);
    
    // Find users who follow the userId
    @Query("SELECT f.followerId FROM Follow f WHERE f.followingId = :userId")
    List<UUID> findFollowerIds(@Param("userId") UUID userId);

    List<Follow> findByFollowerId(UUID followerId);

    List<Follow> findByFollowingId(UUID followingId);

    Follow findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    long countByFollowerId(UUID followerId); // Count following

    long countByFollowingId(UUID followingId); // Count followers

    // Find followers for a user within a specified time period
    List<Follow> findByFollowingIdAndCreatedAtAfterOrderByCreatedAtDesc(UUID followingId, LocalDateTime since);
}