package com.example.repo;

import com.example.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentStatusRepo extends JpaRepository<EnrollmentStatus, Long> {

    Optional<EnrollmentStatus> findByEnrollmentStatus(String status);
}
