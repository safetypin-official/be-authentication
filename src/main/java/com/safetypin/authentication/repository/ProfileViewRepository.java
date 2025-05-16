package com.safetypin.authentication.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.safetypin.authentication.model.ProfileView;

public interface ProfileViewRepository extends JpaRepository<ProfileView, UUID> {
    Optional<ProfileView> findByUser_IdAndViewer_Id(UUID userId, UUID viewerId);

    List<ProfileView> findByUser_Id(UUID userId);

    void deleteByUserId(UUID userId);

    void deleteByViewerId(UUID viewerId);
}
