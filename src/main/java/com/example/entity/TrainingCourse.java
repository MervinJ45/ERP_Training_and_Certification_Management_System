package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "training_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID courseId;

    private String courseName;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private TrainingCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer durationDays;

    private BigDecimal trainingCost;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Employee trainer;

    @ManyToOne
    @JoinColumn(name = "training_type_id")
    private TrainingType trainingType;

    private Boolean certificationProvided;

    private Integer certificationValidityMonths;

    private Integer maxParticipants;

    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt;
}