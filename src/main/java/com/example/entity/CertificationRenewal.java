package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certification_renewals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CertificationRenewal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long renewalId;

    @ManyToOne
    @JoinColumn(name = "certification_id")
    private Certification originalCertification;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDateTime renewalDate;
    private LocalDateTime newExpiryDate;

    @Column(length = 500)
    private String uploadedCertificateUrl;

    @ManyToOne
    @JoinColumn(name = "approval_status_id")
    private ApprovalStatus approvalStatus;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    private LocalDateTime approvalDate;

    @ManyToOne
    @JoinColumn(name = "new_certification_id")
    private Certification newCertification;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}