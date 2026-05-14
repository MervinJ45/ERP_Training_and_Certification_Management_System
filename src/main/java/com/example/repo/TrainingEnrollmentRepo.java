package com.example.repo;

import com.example.entity.TrainingEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingEnrollmentRepo extends JpaRepository<TrainingEnrollment, UUID> {

    boolean existsByEmployeeEmployeeIdAndCourseCourseId(UUID employeeId, UUID courseId);

    List<TrainingEnrollment> findByEmployeeEmployeeId(UUID employeeId);

    Optional<TrainingEnrollment> findByEmployeeManagerEmployeeId(UUID managerId);

    List<TrainingEnrollment> findByEmployeeManagerEmployeeIdAndCurrentApprovalLevelLessThanAndEnrollmentStatusEnrollmentStatus( UUID managerId, Integer level, String status);

    List<TrainingEnrollment> findByEmployeeManagerEmployeeIdAndCurrentApprovalLevelGreaterThan(UUID managerId, int i);
}
