package com.example.repo;

import com.example.entity.SkillMatrix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SkillMatrixRepo extends JpaRepository<SkillMatrix, Long> {

    Optional<SkillMatrix> findByEmployeeEmployeeIdAndCourseCourseId(Long employeeId, Long courseId);

    Collection<SkillMatrix> findByIsActiveTrue();
}
