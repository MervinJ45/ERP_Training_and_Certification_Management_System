package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "approval_workflow_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long configId;

    private java.math.BigDecimal minCost;
    private java.math.BigDecimal maxCost;
    private Integer requiredLevel;

    @ManyToOne
    @JoinColumn(name = "approver_role_id")
    private Role approverRole;

    private String description;
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