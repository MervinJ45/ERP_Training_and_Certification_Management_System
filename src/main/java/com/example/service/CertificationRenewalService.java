package com.example.service;

import com.example.entity.ApprovalStatus;
import com.example.entity.Certification;
import com.example.entity.CertificationRenewal;
import com.example.repo.CertificationRenewalRepo;
import com.example.repo.CertificationRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificationRenewalService {

    private final CertificationRenewalRepo certificationRenewalRepo;
    private final EmployeeService employeeService;
    private final CertificationService certificationService;
    private final ApprovalStatusService approvalStatusService;
    private final CertificationRepo certificationRepo;
    private final AuditLogService auditLogService;

    public CertificationRenewalService(CertificationRenewalRepo certificationRenewalRepo, AuditLogService auditLogService, EmployeeService employeeService, CertificationService certificationService, ApprovalStatusService approvalStatusService, CertificationRepo certificationRepo) {
        this.certificationRenewalRepo = certificationRenewalRepo;
        this.auditLogService = auditLogService;
        this.employeeService = employeeService;
        this.certificationService = certificationService;
        this.approvalStatusService = approvalStatusService;
        this.certificationRepo = certificationRepo;
    }

    public List<CertificationRenewal> getAllRenewals() {
        return certificationRenewalRepo.findAll();
    }

    public CertificationRenewal saveRenewal(CertificationRenewal renewal) {
        boolean isUpdate = renewal.getRenewalId() != null;

        CertificationRenewal savedRenewal = certificationRenewalRepo.save(renewal);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " certification renewal record.";

        auditLogService.logAudit(savedRenewal.getRenewalId(), action, "certification_renewals", details);

        return savedRenewal;
    }

    public void deleteRenewal(Long id) {
        certificationRenewalRepo.findById(id).ifPresent(renewal -> {
            certificationRenewalRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "certification_renewals", "Deleted certification renewal record ID: " + id);
        });
    }

    public List<CertificationRenewal> getPendingRenewals() {
        return certificationRenewalRepo.findByApprovalStatus_approvalStatusId(1L);
    }

    @Transactional
    public void processApprovalDecision(Long renewalId, Long directorEmployeeId, boolean isApproved, String directorRemarks) {
        CertificationRenewal renewal = certificationRenewalRepo.findById(renewalId).orElseThrow(() -> new IllegalArgumentException("Renewal request record not found ID: " + renewalId));

        LocalDateTime now = LocalDateTime.now();

        renewal.setApprovedBy(employeeService.getEmployeeById(directorEmployeeId));
        renewal.setApprovalDate(now);
        renewal.setRemarks(directorRemarks);
        renewal.setUpdatedAt(now);

        if (isApproved) {
            renewal.setApprovalStatus(approvalStatusService.getApprovalStatusById(2L));
            Certification primaryCert = renewal.getOriginalCertification();

            int validityMonths = primaryCert.getCourse().getCertificationValidityMonths();
            LocalDateTime newExpiry = now.plusMonths(validityMonths);

            primaryCert.setExpiryDate(newExpiry);
            primaryCert.setCertificateUrl(renewal.getUploadedCertificateUrl());
            primaryCert.setUpdatedAt(now);
            certificationRepo.save(primaryCert);

            renewal.setNewExpiryDate(newExpiry);
            renewal.setNewCertification(primaryCert);
        } else {
            renewal.setApprovalStatus(approvalStatusService.getApprovalStatusById(3L));
        }

        certificationRenewalRepo.save(renewal);
    }

    @Transactional
    public void submitRenewalRequest(Long certificationId, Long employeeId, String cloudinaryUrl, String remarks) {
        CertificationRenewal renewal = new CertificationRenewal();

        renewal.setOriginalCertification(certificationService.getCertificateById(certificationId));
        renewal.setEmployee(employeeService.getEmployeeById(employeeId));
        renewal.setUploadedCertificateUrl(cloudinaryUrl);
        renewal.setRemarks(remarks);

        renewal.setRenewalDate(LocalDateTime.now());
        renewal.setApprovalStatus(approvalStatusService.getApprovalStatusById(1L));
        renewal.setCreatedAt(LocalDateTime.now());
        renewal.setUpdatedAt(LocalDateTime.now());
        renewal.setIsActive(true);

        certificationRenewalRepo.save(renewal);
    }

    public CertificationRenewal getRenewalById(Long id) {
        return certificationRenewalRepo.findById(id).orElse(null);
    }
}