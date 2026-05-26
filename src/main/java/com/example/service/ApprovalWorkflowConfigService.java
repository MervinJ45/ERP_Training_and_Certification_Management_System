package com.example.service;

import com.example.dto.ApprovalWorkflowConfigDTO;
import com.example.entity.ApprovalWorkflowConfig;
import com.example.repo.ApprovalWorkflowConfigRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalWorkflowConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalWorkflowConfigService.class);

    private final ApprovalWorkflowConfigRepo approvalWorkflowConfigRepo;
    private final AuditLogService auditLogService;

    public ApprovalWorkflowConfigService(ApprovalWorkflowConfigRepo approvalWorkflowConfigRepo, AuditLogService auditLogService) {
        this.approvalWorkflowConfigRepo = approvalWorkflowConfigRepo;
        this.auditLogService = auditLogService;
    }

    public List<ApprovalWorkflowConfig> getAllConfigs() {
        logger.info("Fetching all approval workflow configurations");
        return approvalWorkflowConfigRepo.findAll();
    }


    public List<ApprovalWorkflowConfigDTO> getAllConfigsAsDTOs() {
        logger.info("Fetching and mapping all approval workflow configurations to DTOs");
        return approvalWorkflowConfigRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ApprovalWorkflowConfig saveConfig(ApprovalWorkflowConfig config) {
        boolean isUpdate = config.getConfigId() != null;

        ApprovalWorkflowConfig savedConfig = approvalWorkflowConfigRepo.save(config);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " workflow configuration step.";

        auditLogService.logAudit(savedConfig.getConfigId(), action, "approval_workflow_configs", details);

        logger.info("{} approval workflow configuration with ID: {}", isUpdate ? "Updated" : "Created", savedConfig.getConfigId());

        return savedConfig;
    }

    public void deleteConfig(Long id) {
        approvalWorkflowConfigRepo.findById(id).ifPresent(config -> {
            logger.warn("Deleting approval workflow configuration with ID: {}", id);

            approvalWorkflowConfigRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "approval_workflow_configs", "Deleted approval workflow configuration step ID: " + id);
        });
    }

    public ApprovalWorkflowConfig getConfigById(Long id) {
        logger.info("Fetching approval workflow configuration by ID: {}", id);
        return approvalWorkflowConfigRepo.findById(id).orElse(null);
    }

    public ApprovalWorkflowConfigDTO convertToDTO(ApprovalWorkflowConfig entity) {
        if (entity == null) {
            return null;
        }

        ApprovalWorkflowConfigDTO dto = new ApprovalWorkflowConfigDTO();
        dto.setConfigId(entity.getConfigId());
        dto.setMinCost(entity.getMinCost());
        dto.setMaxCost(entity.getMaxCost());
        dto.setRequiredLevel(entity.getRequiredLevel());

        if (entity.getApproverRole() != null) {
            dto.setApproverRoleName(entity.getApproverRole().getRoleName());
        } else {
            dto.setApproverRoleName("N/A");
        }

        dto.setDescription(entity.getDescription());
        dto.setIsActive(entity.getIsActive());

        return dto;
    }

    public long calculateTotalLevelsForCost(BigDecimal requestedCost) {
        if (requestedCost == null) {
            return 0;
        }

        return getAllConfigs().stream().filter(config -> Boolean.TRUE.equals(config.getIsActive())).filter(config -> {
            java.math.BigDecimal min = config.getMinCost() != null ? config.getMinCost() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal max = config.getMaxCost() != null ? config.getMaxCost() : new java.math.BigDecimal("999999999.99");

            return requestedCost.compareTo(min) >= 0 && requestedCost.compareTo(max) <= 0;
        }).count();
    }
}