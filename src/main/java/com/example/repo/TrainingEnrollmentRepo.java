package com.example.repo;

import com.example.entity.TrainingEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrainingEnrollmentRepo extends JpaRepository<TrainingEnrollment, UUID> {

}
