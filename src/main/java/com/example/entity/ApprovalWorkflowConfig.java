package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "approval_workflow_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID configId;

    private BigDecimal minCost;

    private BigDecimal maxCost;

    private Integer requiredLevel;

    @ManyToOne
    @JoinColumn(name = "approver_role_id")
    private Role approverRole;

    private String description;

    private Boolean isActive;
}