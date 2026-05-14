package com.example.repo;

import com.example.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnrollmentStatusRepo extends JpaRepository<EnrollmentStatus, UUID> {

}
