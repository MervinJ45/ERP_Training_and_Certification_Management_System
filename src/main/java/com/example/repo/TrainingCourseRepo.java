package com.example.repo;

import com.example.entity.TrainingCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingCourseRepo extends JpaRepository<TrainingCourse, Long> {

}
