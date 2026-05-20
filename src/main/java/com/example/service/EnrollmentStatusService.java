package com.example.service;

import com.example.entity.EnrollmentStatus;
import com.example.repo.EnrollmentStatusRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentStatusService {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentStatusService.class);

    private final EnrollmentStatusRepo enrollmentStatusRepo;
    private final AuditLogService auditLogService;

    public EnrollmentStatusService(EnrollmentStatusRepo enrollmentStatusRepo, AuditLogService auditLogService) {
        this.enrollmentStatusRepo = enrollmentStatusRepo;
        this.auditLogService = auditLogService;
    }

    public List<EnrollmentStatus> getAllStatuses() {

        logger.info("Fetching all enrollment statuses");

        return enrollmentStatusRepo.findAll();
    }

    public EnrollmentStatus saveStatus(EnrollmentStatus status) {

        boolean isUpdate = status.getEnrollmentStatusId() != null;

        logger.info("{} operation started for enrollment status: {}", isUpdate ? "UPDATE" : "CREATE", status.getEnrollmentStatus());

        EnrollmentStatus savedStatus = enrollmentStatusRepo.save(status);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " enrollment status: " + savedStatus.getEnrollmentStatus();

        auditLogService.logAudit(savedStatus.getEnrollmentStatusId(), action, "ENROLLMENT_STATUS", details);

        logger.info("Enrollment status saved successfully with id: {}", savedStatus.getEnrollmentStatusId());

        return savedStatus;
    }

    public void deleteStatus(Long id) {

        logger.info("Deleting enrollment status id: {}", id);

        enrollmentStatusRepo.findById(id).ifPresent(status -> {

            enrollmentStatusRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "ENROLLMENT_STATUS", "Deleted enrollment status: " + status.getEnrollmentStatus());

            logger.info("Enrollment status deleted successfully: {}", status.getEnrollmentStatus());
        });
    }

    public EnrollmentStatus getStatusById(Long id) {

        logger.info("Fetching enrollment status by id: {}", id);

        return enrollmentStatusRepo.findById(id).orElse(null);
    }
}