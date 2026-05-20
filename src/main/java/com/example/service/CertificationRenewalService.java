package com.example.service;

import com.example.entity.ApprovalStatus;
import com.example.entity.Certification;
import com.example.entity.CertificationRenewal;
import com.example.repo.CertificationRenewalRepo;
import com.example.repo.CertificationRepo;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificationRenewalService {

    private static final Logger logger = LoggerFactory.getLogger(CertificationRenewalService.class);

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
        logger.info("Fetching all certification renewals");
        return certificationRenewalRepo.findAll();
    }

    public CertificationRenewal saveRenewal(CertificationRenewal renewal) {
        boolean isUpdate = renewal.getRenewalId() != null;

        CertificationRenewal savedRenewal = certificationRenewalRepo.save(renewal);

        String action = isUpdate ? "UPDATE" : "INSERT";
        String details = (isUpdate ? "Updated" : "Created") + " certification renewal record.";

        auditLogService.logAudit(savedRenewal.getRenewalId(), action, "certification_renewals", details);

        logger.info("{} certification renewal with ID: {}", isUpdate ? "Updated" : "Created", savedRenewal.getRenewalId());

        return savedRenewal;
    }

    public void deleteRenewal(Long id) {
        certificationRenewalRepo.findById(id).ifPresent(renewal -> {

            logger.warn("Deleting certification renewal with ID: {}", id);

            certificationRenewalRepo.deleteById(id);

            auditLogService.logAudit(id, "DELETE", "certification_renewals", "Deleted certification renewal record ID: " + id);
        });
    }

    public List<CertificationRenewal> getPendingRenewals() {
        logger.info("Fetching pending certification renewals");
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
            Certification oldCert = renewal.getOriginalCertification();

            int validityMonths = oldCert.getCourse().getCertificationValidityMonths();
            LocalDateTime newExpiry = now.plusMonths(validityMonths);

            Certification newCert = new Certification();
            newCert.setCertificateNumber(oldCert.getCertificateNumber());
            newCert.setCourse(oldCert.getCourse());
            newCert.setEmployee(oldCert.getEmployee());
            newCert.setIssueDate(now);
            newCert.setExpiryDate(newExpiry);
            newCert.setCertificateUrl(renewal.getUploadedCertificateUrl());
            newCert.setCreatedAt(now);
            newCert.setUpdatedAt(now);
            newCert.setIsActive(true);

            Certification savedNewCert = certificationRepo.save(newCert);

            renewal.setNewExpiryDate(newExpiry);
            renewal.setNewCertification(savedNewCert);

            auditLogService.logAudit(renewal.getRenewalId(), "APPROVE", "certification_renewals", "Approved certification renewal for certificate: " + oldCert.getCertificateNumber());

            logger.info("Approved certification renewal ID: {}", renewalId);

        } else {
            renewal.setApprovalStatus(approvalStatusService.getApprovalStatusById(3L));

            auditLogService.logAudit(renewal.getRenewalId(), "REJECT", "certification_renewals", "Rejected certification renewal request");

            logger.warn("Rejected certification renewal ID: {}", renewalId);
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

        CertificationRenewal savedRenewal = certificationRenewalRepo.save(renewal);

        auditLogService.logAudit(savedRenewal.getRenewalId(), "INSERT", "certification_renewals", "Submitted certification renewal request");

        logger.info("Submitted certification renewal request with ID: {}", savedRenewal.getRenewalId());
    }

    public CertificationRenewal getRenewalById(Long id) {
        logger.info("Fetching certification renewal by ID: {}", id);
        return certificationRenewalRepo.findById(id).orElse(null);
    }
}