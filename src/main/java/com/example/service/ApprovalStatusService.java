package com.example.service;

import com.example.entity.ApprovalStatus;
import com.example.repo.ApprovalStatusRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalStatusService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalStatusService.class);

    private final ApprovalStatusRepo approvalStatusRepo;
    private final AuditLogService auditLogService;

    public ApprovalStatusService(ApprovalStatusRepo approvalStatusRepo, AuditLogService auditLogService) {
        this.approvalStatusRepo = approvalStatusRepo;
        this.auditLogService = auditLogService;
    }

    public List<ApprovalStatus> getAllApprovalStatuses() {

        logger.info("Fetching all approval statuses");

        return approvalStatusRepo.findAll();
    }

    public ApprovalStatus saveApprovalStatus(ApprovalStatus approvalStatus) {
        boolean isUpdate = approvalStatus.getApprovalStatusId() != null;

        ApprovalStatus savedStatus = approvalStatusRepo.save(approvalStatus);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " approval status: " + savedStatus.getApprovalStatus();

        auditLogService.logAudit(savedStatus.getApprovalStatusId(), action, "approval_statuses", details);

        logger.info("{} approval status with ID: {}", isUpdate ? "Updated" : "Created", savedStatus.getApprovalStatusId());

        return savedStatus;
    }

    public void deleteApprovalStatus(Long id) {
        approvalStatusRepo.findById(id).ifPresent(status -> {

            logger.warn("Deleting approval status with ID: {}", id);

            approvalStatusRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "approval_statuses", "Deleted approval status: " + status.getApprovalStatus());
        });
    }

    public ApprovalStatus getApprovalStatusById(Long id) {

        logger.info("Fetching approval status by ID: {}", id);

        return approvalStatusRepo.findById(id).orElse(null);
    }
}