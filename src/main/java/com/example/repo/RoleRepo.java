package com.example.repo;

import com.example.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepo extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
}
