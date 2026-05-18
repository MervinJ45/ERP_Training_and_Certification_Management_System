package com.example.service;

import com.example.entity.CertificationStatus;
import com.example.repo.CertificationStatusRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificationStatusService {

    private final CertificationStatusRepo certificationStatusRepo;
    // Injected central AuditLogService
    private final AuditLogService auditLogService;

    public CertificationStatusService(CertificationStatusRepo certificationStatusRepo,
                                      AuditLogService auditLogService) {
        this.certificationStatusRepo = certificationStatusRepo;
        this.auditLogService = auditLogService;
    }

    public List<CertificationStatus> getAllStatuses() {
        return certificationStatusRepo.findAll();
    }

    public CertificationStatus saveStatus(CertificationStatus status) {
        // Determine whether this action is an update or an insertion
        boolean isUpdate = status.getCertificationStatusId() != null;

        CertificationStatus savedStatus = certificationStatusRepo.save(status);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " certification status: " + savedStatus.getCertificationStatus();

        auditLogService.logAudit(
                savedStatus.getCertificationStatusId(),
                action,
                "certification_statuses",
                details
        );

        return savedStatus;
    }

    public void deleteStatus(Long id) {
        // Retrieve the record first to safely log contextual detail information right before deletion
        certificationStatusRepo.findById(id).ifPresent(status -> {
            certificationStatusRepo.deleteById(id);

            auditLogService.logAudit(
                    id,
                    "DELETE",
                    "certification_statuses",
                    "Deleted certification status: " + status.getCertificationStatus()
            );
        });
    }

    public CertificationStatus getStatusById(Long id) {
        return certificationStatusRepo.findById(id).orElse(null);
    }
}