package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "training_enrollments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TrainingEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrollmentId;

    @ManyToOne @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne @JoinColumn(name = "course_id")
    private TrainingCourse course;

    private LocalDateTime enrollmentDate;

    private LocalDateTime completionDate;

    @ManyToOne @JoinColumn(name = "enrollment_status_id")
    private EnrollmentStatus enrollmentStatus;

    private BigDecimal requestedCost;
    private BigDecimal approvedCost;
    private Integer currentApprovalLevel;
    private Boolean certificateIssued;
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}