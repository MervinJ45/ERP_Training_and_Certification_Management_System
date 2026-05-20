package com.example.service;

import com.example.entity.ApprovalWorkflowConfig;
import com.example.repo.ApprovalWorkflowConfigRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

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
}