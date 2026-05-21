package com.example.schedular;

import com.example.entity.Certification;
import com.example.entity.CertificationStatus;
import com.example.repo.CertificationRepo;
import com.example.repo.CertificationStatusRepo;
import com.example.service.AuditLogService;
import com.example.service.CertificateExpiryEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificationExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CertificationExpiryScheduler.class);

    private final CertificationRepo certificationRepository;
    private final CertificationStatusRepo statusRepository;
    private final CertificateExpiryEmailService emailService;
    private final AuditLogService auditLogService;

    public CertificationExpiryScheduler(CertificationRepo certificationRepository, CertificationStatusRepo statusRepository, CertificateExpiryEmailService emailService, AuditLogService auditLogService) {
        this.certificationRepository = certificationRepository;
        this.statusRepository = statusRepository;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    @Scheduled(cron = "0 57 12 * * ?")
    @Transactional
    public void processExpiredCertificates() {
        logger.info("Starting automated certificate expiry check job...");

        LocalDateTime now = LocalDateTime.now();

        CertificationStatus expiredStatus = statusRepository.findById(2L).orElseThrow(() -> {
            logger.error("Expired status configuration (ID: 2L) not found in database!");
            return new IllegalStateException("Expired status configuration not found in DB");
        });

        List<Certification> certificates = certificationRepository.findCertificatesToProcess(now, expiredStatus);
        logger.info("Found {} certificate records requiring processing.", certificates.size());

        int successCount = 0;
        int failureCount = 0;

        for (Certification cert : certificates) {
            try {
                boolean isAlreadyMarkedExpired = cert.getStatus().getCertificationStatusId().equals(expiredStatus.getCertificationStatusId());
                boolean dbStateChanged = false;

                if (isAlreadyMarkedExpired) {
                    logger.info("Certificate ID: {} is already marked expired. Sending follow-up email reminder.", cert.getCertificationId());
                } else {
                    cert.setStatus(expiredStatus);
                    dbStateChanged = true;
                    logger.warn("Certificate ID: {} has passed its expiry date ({}). Changing status to Expired.", cert.getCertificationId(), cert.getExpiryDate());
                }

                emailService.sendExpiryEmail(cert.getEmployee().getEmail(), cert.getEmployee().getFirstName(), cert.getCourse().getCourseName(), cert.getCertificateNumber());

                if (dbStateChanged) {
                    Certification savedCert = certificationRepository.save(cert);

                    auditLogService.logAudit(savedCert.getCertificationId(), "UPDATE", "certifications", "Certificate validity passed expiry date. Automatically changed status to Expired. Certificate Number: " + savedCert.getCertificateNumber());
                    logger.info("Database updated and audit log written for Certificate ID: {}", savedCert.getCertificationId());
                }

                successCount++;

            } catch (Exception e) {
                failureCount++;
                logger.error("Failed to process expiration routine for Certificate ID: {}. Error reason: {}", cert.getCertificationId(), e.getMessage(), e);
            }
        }

        logger.info("Certificate expiry check job completed. Processing Summary: Total processed: {}, Successfully updated/notified: {}, Failures: {}", certificates.size(), successCount, failureCount);
    }
}