package com.example.service;

import com.example.dto.TrainingApprovalDTO;
import com.example.entity.TrainingApproval;
import com.example.repo.TrainingApprovalRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingApprovalService.class);

    private final TrainingApprovalRepo trainingApprovalRepo;
    private final AuditLogService auditLogService;
    private final TrainingApprovalRepo approvalRepo;

    public TrainingApprovalService(TrainingApprovalRepo trainingApprovalRepo, AuditLogService auditLogService, TrainingApprovalRepo approvalRepo) {
        this.trainingApprovalRepo = trainingApprovalRepo;
        this.auditLogService = auditLogService;
        this.approvalRepo = approvalRepo;
    }

    public List<TrainingApproval> getAllApprovals() {

        logger.info("Fetching all training approvals");

        return trainingApprovalRepo.findAll();
    }

    public TrainingApproval saveApproval(TrainingApproval approval) {

        boolean isUpdate = approval.getApprovalId() != null;

        logger.info("{} operation started for training approval", isUpdate ? "UPDATE" : "CREATE");

        TrainingApproval savedApproval = trainingApprovalRepo.save(approval);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " training approval record.";

        auditLogService.logAudit(savedApproval.getApprovalId(), action, "training_approvals", details);

        logger.info("Training approval saved successfully with id: {}", savedApproval.getApprovalId());

        return savedApproval;
    }

    public void deleteApproval(Long id) {

        logger.info("Deleting training approval id: {}", id);

        trainingApprovalRepo.findById(id).ifPresent(approval -> {

            trainingApprovalRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "training_approvals", "Deleted training approval record ID: " + id);

            logger.info("Training approval deleted successfully with id: {}", id);
        });
    }

    public List<TrainingApprovalDTO> getApprovalsByApprover(Long employeeId) {

        logger.info("Fetching approvals for approver employee id: {}", employeeId);

        List<TrainingApproval> approvals = approvalRepo.findByApprover_EmployeeIdAndIsActiveTrue(employeeId);

        return approvals.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TrainingApproval getApprovalById(Long id) {

        logger.info("Fetching training approval by id: {}", id);

        return trainingApprovalRepo.findById(id).orElse(null);
    }

    public List<TrainingApproval> getApprovalsByEnrollmentId(Long enrollmentId) {
        logger.info("Fetching validation approval workflow records for enrollment ID: {}", enrollmentId);

        return trainingApprovalRepo.findAll().stream().filter(approval -> approval.getEnrollment() != null && approval.getEnrollment().getEnrollmentId().equals(enrollmentId)).collect(Collectors.toList());
    }

    private TrainingApprovalDTO convertToDTO(TrainingApproval approval) {
        return TrainingApprovalDTO.builder().enrollmentId(approval.getEnrollment().getEnrollmentId()).courseName(approval.getEnrollment().getCourse().getCourseName()).employeeFullName(approval.getEnrollment().getEmployee().getFirstName() + " " + approval.getEnrollment().getEmployee().getLastName()).approvalLevel(approval.getApprovalLevel()).comments(approval.getComments()).approvalStatusName(approval.getApprovalStatus().getApprovalStatus()).actionDate(approval.getActionDate()).build();
    }
}