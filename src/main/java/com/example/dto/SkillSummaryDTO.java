package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SkillSummaryDTO {
    private Long skillId;
    private String employeeFullName;
    private String courseName;
    private String skillName;
    private Integer proficiencyRating;
    private LocalDateTime updatedAt;
}