package com.example.service;

import com.example.entity.CertificationStatus;
import com.example.repo.CertificationStatusRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificationStatusService {

    private static final Logger logger = LoggerFactory.getLogger(CertificationStatusService.class);

    private final CertificationStatusRepo certificationStatusRepo;
    private final AuditLogService auditLogService;

    public CertificationStatusService(CertificationStatusRepo certificationStatusRepo, AuditLogService auditLogService) {
        this.certificationStatusRepo = certificationStatusRepo;
        this.auditLogService = auditLogService;
    }

    public List<CertificationStatus> getAllStatuses() {

        logger.info("Fetching all certification statuses");

        return certificationStatusRepo.findAll();
    }

    public CertificationStatus saveStatus(CertificationStatus status) {

        boolean isUpdate = status.getCertificationStatusId() != null;

        logger.info("{} operation started for certification status: {}", isUpdate ? "UPDATE" : "CREATE", status.getCertificationStatus());

        CertificationStatus savedStatus = certificationStatusRepo.save(status);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " certification status: " + savedStatus.getCertificationStatus();

        auditLogService.logAudit(savedStatus.getCertificationStatusId(), action, "CERTIFICATION_STATUS", details);

        logger.info("Certification status saved successfully with id: {}", savedStatus.getCertificationStatusId());

        return savedStatus;
    }

    public void deleteStatus(Long id) {

        logger.info("Deleting certification status id: {}", id);

        certificationStatusRepo.findById(id).ifPresent(status -> {

            certificationStatusRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "CERTIFICATION_STATUS", "Deleted certification status: " + status.getCertificationStatus());

            logger.info("Certification status deleted successfully: {}", status.getCertificationStatus());
        });
    }

    public CertificationStatus getStatusById(Long id) {

        logger.info("Fetching certification status by id: {}", id);

        return certificationStatusRepo.findById(id).orElse(null);
    }

    public String getBadgeColorByStatusId(String statusName) {
        if (statusName == null) {
            return "background-color: #94A3B8; color: white;";
        }

        return switch (statusName) {
            case "Active" -> "background-color: #DCFCE7; color: #15803D;";
            case "Expired" -> "background-color: #FEE2E2; color: #B91C1C;";
            case "Renewed" -> "background-color: #FFEDD5; color: #C2410C;";
            default -> "background-color: #FEF9C3; color: #854D0E;";
        };
    }
}