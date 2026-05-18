package com.example.service;

import com.example.entity.CertificationRenewal;
import com.example.repo.CertificationRenewalRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificationRenewalService {

    private final CertificationRenewalRepo certificationRenewalRepo;
    // Injected central AuditLogService
    private final AuditLogService auditLogService;

    public CertificationRenewalService(CertificationRenewalRepo certificationRenewalRepo,
                                       AuditLogService auditLogService) {
        this.certificationRenewalRepo = certificationRenewalRepo;
        this.auditLogService = auditLogService;
    }

    public List<CertificationRenewal> getAllRenewals() {
        return certificationRenewalRepo.findAll();
    }

    public CertificationRenewal saveRenewal(CertificationRenewal renewal) {
        // Determine whether this is an insert or an update operation
        boolean isUpdate = renewal.getRenewalId() != null;

        CertificationRenewal savedRenewal = certificationRenewalRepo.save(renewal);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " certification renewal record.";

        // Optional: If your entity has a specific name or tracking number, you can append it:
        // e.g., + " For Certificate ID: " + savedRenewal.getCertificationId()

        auditLogService.logAudit(
                savedRenewal.getRenewalId(),
                action,
                "certification_renewals",
                details
        );

        return savedRenewal;
    }

    public void deleteRenewal(Long id) {
        // Locate the record first to safely audit details right before deletion
        certificationRenewalRepo.findById(id).ifPresent(renewal -> {
            certificationRenewalRepo.deleteById(id);

            auditLogService.logAudit(
                    id,
                    "DELETE",
                    "certification_renewals",
                    "Deleted certification renewal record ID: " + id
            );
        });
    }

    public CertificationRenewal getRenewalById(Long id) {
        return certificationRenewalRepo.findById(id).orElse(null);
    }
}