package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    private String courseName;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private TrainingCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer durationDays;
    private java.math.BigDecimal trainingCost;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Employee trainer;

    @ManyToOne
    @JoinColumn(name = "training_type_id")
    private TrainingType trainingType;

    private Boolean certificationProvided;
    private Integer certificationValidityMonths;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User creator;

    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}