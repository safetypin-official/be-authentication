package com.safetypin.authentication.repository;

import com.safetypin.authentication.model.Role;
import com.safetypin.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);

    User findByRole(Role role);
}
