package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "training_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID approvalId;

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

    @Column(columnDefinition = "TEXT")
    private String comments;

    private LocalDateTime actionDate;
}