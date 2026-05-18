package com.example.repo;

import com.example.entity.TrainingEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingEnrollmentRepo extends JpaRepository<TrainingEnrollment, Long> {

    boolean existsByEmployeeEmployeeIdAndCourseCourseId(Long employeeId, Long courseId);

    List<TrainingEnrollment> findByEmployeeEmployeeId(Long employeeId);

    Optional<TrainingEnrollment> findByEmployeeManagerEmployeeId(Long managerId);

    List<TrainingEnrollment> findByEmployeeManagerEmployeeIdAndCurrentApprovalLevelLessThanAndEnrollmentStatusEnrollmentStatus( Long managerId, Integer level, String status);

    List<TrainingEnrollment> findByEmployeeManagerEmployeeIdAndCurrentApprovalLevelGreaterThan(Long managerId, int i);

    List<TrainingEnrollment> findByEnrollmentStatusEnrollmentStatusAndIsActiveTrue(String pending);
}
