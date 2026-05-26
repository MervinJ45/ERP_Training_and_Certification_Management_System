package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCourseDTO {

    private Long courseId;
    private String courseName;
    private String courseDescription;
    private TrainingCategoryDTO category;
    private EmployeeDTO trainer;
    private TrainingTypeDTO trainingType;
    private UserDTO createdBy;
    private String categoryName;
    private String trainerName;
    private String trainingTypeName;
    private String createdByName;
    private String description;
    private Integer durationDays;
    private BigDecimal trainingCost;
    private Boolean certificationProvided;
    private Integer certificationValidityMonths;
    private boolean isActive;
    private LocalDateTime createdAt;
}