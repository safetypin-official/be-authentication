package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.Location;
import com.safetypin.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    Optional<Location> findByUser(User user);
    
    Optional<Location> findByUser_Id(UUID userId);
    
    @Query("SELECT l FROM Location l WHERE l.user.id IN :userIds")
    List<Location> findByUserIdIn(@Param("userIds") List<UUID> userIds);
}