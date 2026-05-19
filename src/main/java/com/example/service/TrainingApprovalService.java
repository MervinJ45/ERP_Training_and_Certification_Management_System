package com.example.service;

import com.example.dto.TrainingApprovalDTO;
import com.example.entity.TrainingApproval;
import com.example.repo.TrainingApprovalRepo;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingApprovalService {

    private final TrainingApprovalRepo trainingApprovalRepo;
    private final AuditLogService auditLogService;
    private final TrainingApprovalRepo approvalRepo;

    public TrainingApprovalService(TrainingApprovalRepo trainingApprovalRepo, AuditLogService auditLogService, TrainingApprovalRepo approvalRepo) {
        this.trainingApprovalRepo = trainingApprovalRepo;
        this.auditLogService = auditLogService;
        this.approvalRepo = approvalRepo;
    }

    public List<TrainingApproval> getAllApprovals() {
        return trainingApprovalRepo.findAll();
    }

    public TrainingApproval saveApproval(TrainingApproval approval) {
        boolean isUpdate = approval.getApprovalId() != null;

        TrainingApproval savedApproval = trainingApprovalRepo.save(approval);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " training approval record.";

        auditLogService.logAudit(savedApproval.getApprovalId(), action, "training_approvals", details);

        return savedApproval;
    }

    public void deleteApproval(Long id) {
        trainingApprovalRepo.findById(id).ifPresent(approval -> {
            trainingApprovalRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "training_approvals", "Deleted training approval record ID: " + id);
        });
    }

    public List<TrainingApprovalDTO> getApprovalsByApprover(Long employeeId) {
        List<TrainingApproval> approvals = approvalRepo.findByApprover_EmployeeIdAndIsActiveTrue(employeeId);

        return approvals.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TrainingApproval getApprovalById(Long id) {
        return trainingApprovalRepo.findById(id).orElse(null);
    }

    private TrainingApprovalDTO convertToDTO(TrainingApproval approval) {
        return TrainingApprovalDTO.builder()
                .enrollmentId(approval.getEnrollment().getEnrollmentId())
                .courseName(approval.getEnrollment().getCourse().getCourseName())
                .employeeFullName(approval.getEnrollment().getEmployee().getFirstName() + " " +
                        approval.getEnrollment().getEmployee().getLastName())
                .approvalLevel(approval.getApprovalLevel())
                .comments(approval.getComments())
                .approvalStatusName(approval.getApprovalStatus().getApprovalStatus())
                .actionDate(approval.getActionDate())
                .build();
    }
}