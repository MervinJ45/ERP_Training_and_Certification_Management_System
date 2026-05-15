package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "training_approvals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TrainingApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approvalId;

    @ManyToOne
    @JoinColumn(name = "enrollment_id")
    private TrainingEnrollment enrollment;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private Employee approver;

    private Integer approvalLevel;

    @ManyToOne
    @JoinColumn(name = "approval_status_id")
    private ApprovalStatus approvalStatus;

    private String comments;
    private LocalDateTime actionDate;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}