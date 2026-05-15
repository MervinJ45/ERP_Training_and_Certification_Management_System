package com.example.repo;

import com.example.entity.TrainingCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingCourseRepo extends JpaRepository<TrainingCourse, Long> {

    Optional<TrainingCourse> findByCourseName(String trainingCourseName);

    @Query("select c from TrainingCourse c " +
            "where lower(c.courseName) like lower(concat('%', :searchTerm, '%'))")
    List<TrainingCourse> searchByCourseName(@Param("searchTerm") String value);
}
