package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID certificationId;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private TrainingCourse course;

    @ManyToOne
    @JoinColumn(name = "enrollment_id")
    private TrainingEnrollment enrollment;

    private String certificateNumber;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    @ManyToOne
    @JoinColumn(name = "certification_status_id")
    private CertificationStatus certificationStatus;

    @ManyToOne
    @JoinColumn(name = "issued_by")
    private User issuedBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(columnDefinition = "TEXT")
    private String certificateUrl;

    private LocalDateTime createdAt;
}