package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certification_renewals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRenewal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID renewalId;

    @ManyToOne
    @JoinColumn(name = "certification_id")
    private Certification certification;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate renewalDate;

    private LocalDate newExpiryDate;

    @Column(columnDefinition = "TEXT")
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

    private LocalDateTime createdAt;
}