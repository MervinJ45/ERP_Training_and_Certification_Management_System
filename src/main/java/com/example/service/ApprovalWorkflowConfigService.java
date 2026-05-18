package com.example.service;

import com.example.entity.ApprovalWorkflowConfig;
import com.example.repo.ApprovalWorkflowConfigRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalWorkflowConfigService {

    private final ApprovalWorkflowConfigRepo approvalWorkflowConfigRepo;
    private final AuditLogService auditLogService;

    public ApprovalWorkflowConfigService(ApprovalWorkflowConfigRepo approvalWorkflowConfigRepo,
                                         AuditLogService auditLogService) {
        this.approvalWorkflowConfigRepo = approvalWorkflowConfigRepo;
        this.auditLogService = auditLogService;
    }

    public List<ApprovalWorkflowConfig> getAllConfigs() {
        return approvalWorkflowConfigRepo.findAll();
    }

    public ApprovalWorkflowConfig saveConfig(ApprovalWorkflowConfig config) {
        boolean isUpdate = config.getConfigId() != null;

        ApprovalWorkflowConfig savedConfig = approvalWorkflowConfigRepo.save(config);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " workflow configuration step.";

        auditLogService.logAudit(
                savedConfig.getConfigId(),
                action,
                "approval_workflow_configs",
                details
        );

        return savedConfig;
    }

    public void deleteConfig(Long id) {
        approvalWorkflowConfigRepo.findById(id).ifPresent(config -> {
            approvalWorkflowConfigRepo.deleteById(id);

            auditLogService.logAudit(
                    id,
                    "DELETE",
                    "approval_workflow_configs",
                    "Deleted approval workflow configuration step ID: " + id
            );
        });
    }

    public ApprovalWorkflowConfig getConfigById(Long id) {
        return approvalWorkflowConfigRepo.findById(id)
                .orElse(null);
    }
}