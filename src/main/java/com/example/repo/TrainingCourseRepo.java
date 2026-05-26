package com.example.repo;

import com.example.entity.TrainingCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingCourseRepo extends JpaRepository<TrainingCourse, Long> {
    List<TrainingCourse> findByIsActiveTrue();
}
