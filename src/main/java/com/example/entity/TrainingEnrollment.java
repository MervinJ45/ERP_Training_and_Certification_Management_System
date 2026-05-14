package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "training_enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID enrollmentId;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private TrainingCourse course;

    private LocalDate enrollmentDate;

    @ManyToOne
    @JoinColumn(name = "enrollment_status_id")
    private EnrollmentStatus enrollmentStatus;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private BigDecimal requestedCost;

    private BigDecimal approvedCost;

    private Integer currentApprovalLevel;

    private LocalDate completionDate;

    private Boolean certificateIssued;

    private LocalDateTime createdAt;
}