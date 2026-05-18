package com.example.dto;

import java.time.LocalDateTime;

public class SkillSummaryDTO {
    private Long skillId;
    private String employeeFullName;
    private String courseName;
    private String skillName;
    private Integer proficiencyRating;
    private LocalDateTime updatedAt;

    public SkillSummaryDTO(Long skillId, String employeeFullName, String courseName,
                           String skillName, Integer proficiencyRating, LocalDateTime updatedAt) {
        this.skillId = skillId;
        this.employeeFullName = employeeFullName;
        this.courseName = courseName;
        this.skillName = skillName;
        this.proficiencyRating = proficiencyRating;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getSkillId() { return skillId; }
    public String getEmployeeFullName() { return employeeFullName; }
    public String getCourseName() { return courseName; }
    public String getSkillName() { return skillName; }
    public Integer getProficiencyRating() { return proficiencyRating; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}