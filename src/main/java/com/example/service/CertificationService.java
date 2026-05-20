package com.example.service;

import com.example.dto.CertificationDisplayDTO;
import com.example.entity.*;
import com.example.repo.CertificationRepo;
import com.example.repo.CertificationStatusRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CertificationService {

    private static final Logger logger = LoggerFactory.getLogger(CertificationService.class);

    private final CertificationRepo certificationRepo;
    private final AuditLogService auditLogService;
    private final CertificationStatusRepo certificationStatusRepo;

    public CertificationService(CertificationRepo certificationRepo, AuditLogService auditLogService, CertificationStatusRepo certificationStatusRepo) {
        this.certificationRepo = certificationRepo;
        this.auditLogService = auditLogService;
        this.certificationStatusRepo = certificationStatusRepo;
    }

    public Certification getCertificateById(Long certificationId) {

        logger.info("Fetching certificate by id: {}", certificationId);

        return certificationRepo.findById(certificationId).orElse(null);
    }

    public List<CertificationDisplayDTO> getAllCertificationDTOs() {

        logger.info("Fetching all certifications");

        return certificationRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<CertificationDisplayDTO> getMyCertifications(Long employeeId) {

        logger.info("Fetching certifications for employee id: {}", employeeId);

        return certificationRepo.findByEmployeeEmployeeIdAndIsActiveTrue(employeeId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public Certification saveCertification(Certification certification) {

        boolean isNew = (certification.getCertificationId() == null);

        logger.info("{} operation started for certification", isNew ? "CREATE" : "UPDATE");

        String action = isNew ? "CREATE_CERTIFICATE" : "UPDATE_CERTIFICATE";

        Certification savedCert = certificationRepo.save(certification);

        String employeeName = savedCert.getEmployee() != null ? savedCert.getEmployee().getFirstName() + " " + savedCert.getEmployee().getLastName() : "N/A";

        String courseName = savedCert.getCourse() != null ? savedCert.getCourse().getCourseName() : "N/A";

        String details = String.format("Serial: %s, Student: %s, Course: %s", savedCert.getCertificateNumber(), employeeName, courseName);

        auditLogService.logAudit(savedCert.getCertificationId(), action, "CERTIFICATIONS", details);

        logger.info("Certification saved successfully with id: {}", savedCert.getCertificationId());

        return savedCert;
    }

    @Transactional
    public void deleteCertification(Long id) {

        logger.info("Deleting certification id: {}", id);

        certificationRepo.findById(id).ifPresent(cert -> {

            auditLogService.logAudit(id, "DELETE_CERTIFICATE", "CERTIFICATIONS", "Deleted certificate serial: " + cert.getCertificateNumber());

            logger.info("Certification deleted successfully with serial: {}", cert.getCertificateNumber());
        });

        certificationRepo.deleteById(id);
    }

    public CertificationDisplayDTO getCertificationDTOById(Long id) {

        logger.info("Fetching certification DTO by id: {}", id);

        Certification cert = certificationRepo.findById(id).orElse(null);

        return (cert != null) ? convertToDTO(cert) : null;
    }

    public Certification getCertificationById(Long id) {

        logger.info("Fetching certification entity by id: {}", id);

        return certificationRepo.findById(id).orElse(null);
    }

    public Optional<Certification> findByCertificateNumber(String certNo) {

        logger.info("Searching certification by certificate number: {}", certNo);

        return certificationRepo.findByCertificateNumber(certNo);
    }

    @Transactional
    public Certification createCertification(TrainingEnrollment enrollment, String remarks, LocalDateTime now) {

        logger.info("Creating certification for enrollment id: {}", enrollment.getEnrollmentId());

        TrainingCourse course = enrollment.getCourse();
        Employee student = enrollment.getEmployee();

        CertificationStatus activeCertStatus = certificationStatusRepo.findByCertificationStatus("Active").orElse(null);

        Certification certificate = new Certification();

        certificate.setEmployee(student);
        certificate.setCourse(course);
        certificate.setEnrollment(enrollment);

        String uniqueSerial = "CE" + enrollment.getEnrollmentId() + student.getEmployeeId();

        if (uniqueSerial.length() > 10) {
            uniqueSerial = uniqueSerial.substring(0, 10);
        }

        certificate.setCertificateNumber(uniqueSerial);

        certificate.setIssueDate(now);
        certificate.setExpiryDate(now.plusMonths(course.getCertificationValidityMonths()));
        certificate.setStatus(activeCertStatus);
        certificate.setIssuedBy(course.getTrainer());
        certificate.setRemarks(remarks);
        certificate.setCreatedAt(now);
        certificate.setUpdatedAt(now);
        certificate.setIsActive(true);

        Certification savedCertificate = certificationRepo.save(certificate);

        auditLogService.logAudit(savedCertificate.getCertificationId(), "INSERT", "CERTIFICATIONS", "Generated certification with serial number: " + savedCertificate.getCertificateNumber());

        logger.info("Certification created successfully with id: {}", savedCertificate.getCertificationId());

        return savedCertificate;
    }

    public List<CertificationDisplayDTO> searchCertificationDTOs(String value) {

        logger.info("Searching certifications with keyword: {}", value);

        String lowerCaseQuery = value.toLowerCase().trim();

        return certificationRepo.findAll().stream().filter(cert -> (cert.getCertificateNumber() != null && cert.getCertificateNumber().toLowerCase().contains(lowerCaseQuery)) || (cert.getCourse() != null && cert.getCourse().getCourseName().toLowerCase().contains(lowerCaseQuery))).map(this::convertToDTO).collect(Collectors.toList());
    }

    public CertificationDisplayDTO convertToDTO(Certification cert) {

        LocalDate today = LocalDate.now();

        LocalDate expiry = cert.getExpiryDate() != null ? cert.getExpiryDate().toLocalDate() : today;

        long daysRemaining = ChronoUnit.DAYS.between(today, expiry);

        String courseName = cert.getCourse() != null ? cert.getCourse().getCourseName() : "N/A";

        String status = cert.getStatus() != null ? cert.getStatus().getCertificationStatus() : "Unknown";

        return CertificationDisplayDTO.builder().certificationId(cert.getCertificationId()).certificateNumber(cert.getCertificateNumber()).courseName(courseName).issueDate(cert.getIssueDate() != null ? cert.getIssueDate().toLocalDate() : null).expiryDate(expiry).daysRemaining(daysRemaining).statusName(status).certificateUrl(cert.getCertificateUrl()).build();
    }
}