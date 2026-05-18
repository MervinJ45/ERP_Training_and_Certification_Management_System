package com.example.service;

import com.example.dto.CertificationDisplayDTO;
import com.example.entity.Certification;
import com.example.repo.CertificationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CertificationService {

    private final CertificationRepo certificationRepo;
    private final AuditLogService auditLogService;

    public CertificationService(CertificationRepo certificationRepo, AuditLogService auditLogService) {
        this.certificationRepo = certificationRepo;
        this.auditLogService = auditLogService;
    }

    public List<CertificationDisplayDTO> getAllCertificationDTOs() {
        return certificationRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CertificationDisplayDTO> getMyCertifications(Long employeeId) {
        return certificationRepo.findByEmployeeEmployeeIdAndIsActiveTrue(employeeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Certification saveCertification(Certification certification) {
        boolean isNew = (certification.getCertificationId() == null);
        String action = isNew ? "CREATE_CERTIFICATE" : "UPDATE_CERTIFICATE";

        Certification savedCert = certificationRepo.save(certification);

        String employeeName = savedCert.getEmployee() != null ?
                savedCert.getEmployee().getFirstName() + " " + savedCert.getEmployee().getLastName() : "N/A";
        String courseName = savedCert.getCourse() != null ? savedCert.getCourse().getCourseName() : "N/A";

        String details = String.format("Serial: %s, Student: %s, Course: %s",
                savedCert.getCertificateNumber(), employeeName, courseName);

        auditLogService.logAudit(
                savedCert.getCertificationId(),
                action,
                "CERTIFICATIONS",
                details
        );

        return savedCert;
    }

    @Transactional
    public void deleteCertification(Long id) {
        certificationRepo.findById(id).ifPresent(cert -> {
            auditLogService.logAudit(
                    id,
                    "DELETE_CERTIFICATE",
                    "CERTIFICATIONS",
                    "Deleted certificate serial: " + cert.getCertificateNumber()
            );
        });
        certificationRepo.deleteById(id);
    }

    public CertificationDisplayDTO getCertificationDTOById(Long id) {
        Certification cert = certificationRepo.findById(id).orElse(null);
        return (cert != null) ? convertToDTO(cert) : null;
    }

    public Certification getCertificationById(Long id) {
        return certificationRepo.findById(id).orElse(null);
    }

    public Optional<Certification> findByCertificateNumber(String certNo) {
        return certificationRepo.findByCertificateNumber(certNo);
    }

    public List<CertificationDisplayDTO> searchCertificationDTOs(String value) {
        String lowerCaseQuery = value.toLowerCase().trim();
        return certificationRepo.findAll().stream()
                .filter(cert ->
                        (cert.getCertificateNumber() != null && cert.getCertificateNumber().toLowerCase().contains(lowerCaseQuery)) ||
                                (cert.getCourse() != null && cert.getCourse().getCourseName().toLowerCase().contains(lowerCaseQuery))
                )
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CertificationDisplayDTO convertToDTO(Certification cert) {
        LocalDate today = LocalDate.now();
        LocalDate expiry = cert.getExpiryDate() != null ? cert.getExpiryDate().toLocalDate() : today;
        long daysRemaining = ChronoUnit.DAYS.between(today, expiry);

        String courseName = cert.getCourse() != null ? cert.getCourse().getCourseName() : "N/A";
        String status = cert.getStatus() != null ? cert.getStatus().getCertificationStatus() : "Unknown";

        return CertificationDisplayDTO.builder()
                .certificationId(cert.getCertificationId())
                .certificateNumber(cert.getCertificateNumber())
                .courseName(courseName)
                .issueDate(cert.getIssueDate() != null ? cert.getIssueDate().toLocalDate() : null)
                .expiryDate(expiry)
                .daysRemaining(daysRemaining)
                .statusName(status)
                .certificateUrl(cert.getCertificateUrl())
                .build();
    }
}