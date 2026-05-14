package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "skill_matrix")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID skillId;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private TrainingCourse course;

    private String skillName;

    private Integer proficiencyRating;

    private LocalDateTime lastUpdated;
}