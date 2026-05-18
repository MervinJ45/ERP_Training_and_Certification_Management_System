package com.example.service;

import com.example.entity.ApprovalStatus;
import com.example.repo.ApprovalStatusRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalStatusService {

    private final ApprovalStatusRepo approvalStatusRepo;
    private final AuditLogService auditLogService;

    public ApprovalStatusService(ApprovalStatusRepo approvalStatusRepo, AuditLogService auditLogService) {
        this.approvalStatusRepo = approvalStatusRepo;
        this.auditLogService = auditLogService;
    }

    public List<ApprovalStatus> getAllApprovalStatuses() {
        return approvalStatusRepo.findAll();
    }

    public ApprovalStatus saveApprovalStatus(ApprovalStatus approvalStatus) {
        boolean isUpdate = approvalStatus.getApprovalStatusId() != null;

        ApprovalStatus savedStatus = approvalStatusRepo.save(approvalStatus);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " approval status: " + savedStatus.getApprovalStatus();

        auditLogService.logAudit(
                savedStatus.getApprovalStatusId(),
                action,
                "approval_statuses",
                details
        );

        return savedStatus;
    }

    public void deleteApprovalStatus(Long id) {
        approvalStatusRepo.findById(id).ifPresent(status -> {
            approvalStatusRepo.deleteById(id);

            auditLogService.logAudit(
                    id,
                    "DELETE",
                    "approval_statuses",
                    "Deleted approval status: " + status.getApprovalStatus()
            );
        });
    }

    public ApprovalStatus getApprovalStatusById(Long id) {
        return approvalStatusRepo.findById(id).orElse(null);
    }
}