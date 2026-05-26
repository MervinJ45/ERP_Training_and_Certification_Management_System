package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflowConfigDTO {
    private Long configId;
    private BigDecimal minCost;
    private BigDecimal maxCost;
    private Integer requiredLevel;
    private String approverRoleName;
    private String description;
    private Boolean isActive;
}