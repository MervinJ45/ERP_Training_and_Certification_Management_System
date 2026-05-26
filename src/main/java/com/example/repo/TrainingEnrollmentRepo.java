package com.example.repo;

import com.example.entity.TrainingEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingEnrollmentRepo extends JpaRepository<TrainingEnrollment, Long> {

    boolean existsByEmployeeEmployeeIdAndCourseCourseId(Long employeeId, Long courseId);
    List<TrainingEnrollment> findByEmployeeEmployeeId(Long employeeId);
    List<TrainingEnrollment> findByEmployeeManagerEmployeeId(Long managerId);
    List<TrainingEnrollment> findByEnrollmentStatusEnrollmentStatusAndIsActiveTrue(String pending);
}
