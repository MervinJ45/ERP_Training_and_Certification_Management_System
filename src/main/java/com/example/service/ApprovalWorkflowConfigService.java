package com.example.service;

import com.example.entity.ApprovalWorkflowConfig;
import com.example.repo.ApprovalWorkflowConfigRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ApprovalWorkflowConfigService {

    private final ApprovalWorkflowConfigRepo
            approvalWorkflowConfigRepo;

    public ApprovalWorkflowConfigService(ApprovalWorkflowConfigRepo approvalWorkflowConfigRepo) {
        this.approvalWorkflowConfigRepo = approvalWorkflowConfigRepo;
    }

    public List<ApprovalWorkflowConfig> getAllConfigs() {
        return approvalWorkflowConfigRepo.findAll();
    }

    public ApprovalWorkflowConfig saveConfig(ApprovalWorkflowConfig config) {
        return approvalWorkflowConfigRepo.save(config);
    }

    public void deleteConfig(UUID id) {
        approvalWorkflowConfigRepo.deleteById(id);
    }

    public ApprovalWorkflowConfig getConfigById(UUID id) {
        return approvalWorkflowConfigRepo.findById(id)
                .orElse(null);
    }
}