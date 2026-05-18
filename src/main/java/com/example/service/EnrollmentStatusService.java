package com.example.service;

import com.example.entity.EnrollmentStatus;
import com.example.repo.EnrollmentStatusRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentStatusService {

    private final EnrollmentStatusRepo enrollmentStatusRepo;
    // Injected central AuditLogService
    private final AuditLogService auditLogService;

    public EnrollmentStatusService(EnrollmentStatusRepo enrollmentStatusRepo, AuditLogService auditLogService) {
        this.enrollmentStatusRepo = enrollmentStatusRepo;
        this.auditLogService = auditLogService;
    }

    public List<EnrollmentStatus> getAllStatuses() {
        return enrollmentStatusRepo.findAll();
    }

    public EnrollmentStatus saveStatus(EnrollmentStatus status) {
        // Determine whether this action is an update or an insertion
        boolean isUpdate = status.getEnrollmentStatusId() != null;

        EnrollmentStatus savedStatus = enrollmentStatusRepo.save(status);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " enrollment status: " + savedStatus.getEnrollmentStatus();

        auditLogService.logAudit(
                savedStatus.getEnrollmentStatusId(),
                action,
                "enrollment_statuses",
                details
        );

        return savedStatus;
    }

    public void deleteStatus(Long id) {
        // Retrieve the record first to safely log contextual details right before deletion
        enrollmentStatusRepo.findById(id).ifPresent(status -> {
            enrollmentStatusRepo.deleteById(id);

            auditLogService.logAudit(
                    id,
                    "DELETE",
                    "enrollment_statuses",
                    "Deleted enrollment status: " + status.getEnrollmentStatus()
            );
        });
    }

    public EnrollmentStatus getStatusById(Long id) {
        return enrollmentStatusRepo.findById(id).orElse(null);
    }
}