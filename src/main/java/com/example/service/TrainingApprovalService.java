package com.example.service;

import com.example.entity.TrainingApproval;
import com.example.repo.TrainingApprovalRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingApprovalService {

    private final TrainingApprovalRepo trainingApprovalRepo;
    // Injected central AuditLogService
    private final AuditLogService auditLogService;

    public TrainingApprovalService(TrainingApprovalRepo trainingApprovalRepo, AuditLogService auditLogService) {
        this.trainingApprovalRepo = trainingApprovalRepo;
        this.auditLogService = auditLogService;
    }

    public List<TrainingApproval> getAllApprovals() {
        return trainingApprovalRepo.findAll();
    }

    public TrainingApproval saveApproval(TrainingApproval approval) {
        // Determine whether this action is an update or an insertion
        boolean isUpdate = approval.getApprovalId() != null;

        TrainingApproval savedApproval = trainingApprovalRepo.save(approval);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " training approval record.";

        // Optional: If your approval entity maps context strings (e.g., status, course name), you can append them:

        auditLogService.logAudit(
                savedApproval.getApprovalId(),
                action,
                "training_approvals",
                details
        );

        return savedApproval;
    }

    public void deleteApproval(Long id) {
        trainingApprovalRepo.findById(id).ifPresent(approval -> {
            trainingApprovalRepo.deleteById(id);

            auditLogService.logAudit(
                    id,
                    "DELETE",
                    "training_approvals",
                    "Deleted training approval record ID: " + id
            );
        });
    }

    public TrainingApproval getApprovalById(Long id) {
        return trainingApprovalRepo.findById(id).orElse(null);
    }
}