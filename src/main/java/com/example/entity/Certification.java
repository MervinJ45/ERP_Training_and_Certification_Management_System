package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificationId;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private TrainingCourse course;

    @OneToOne
    @JoinColumn(name = "enrollment_id")
    private TrainingEnrollment enrollment;

    private String certificateNumber;
    private LocalDateTime issueDate;
    private LocalDateTime expiryDate;

    @ManyToOne
    @JoinColumn(name = "certification_status_id")
    private CertificationStatus status;

    @ManyToOne
    @JoinColumn(name = "issued_by")
    private Employee issuedBy;

    private String remarks;
    private String certificateUrl;
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