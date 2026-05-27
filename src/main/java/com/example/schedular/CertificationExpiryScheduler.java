package com.example.schedular;

import com.example.entity.Certification;
import com.example.entity.Employee;
import com.example.repo.CertificationRepo;
import com.example.service.CertificateExpiryEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CertificationExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CertificationExpiryScheduler.class);

    private final CertificationRepo certificationRepository;
    private final CertificateExpiryEmailService emailService;

    public CertificationExpiryScheduler(CertificationRepo certificationRepository, CertificateExpiryEmailService emailService) {
        this.certificationRepository = certificationRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 43 16 * * ?")
    @Transactional
    public void processUpcomingExpiryReminders() {
        logger.info("Starting automated course-duration and day-before expiry reminder job...");

        LocalDate today = LocalDate.now();

        List<Certification> allCertificates = certificationRepository.findAll();

        if (allCertificates.isEmpty()) {
            logger.info("No certificate records found in the system.");
            return;
        }

        List<Certification> certificatesToProcess = allCertificates.stream()
                .filter(cert -> cert.getExpiryDate() != null)
                .filter(cert -> cert.getEnrollment() != null && cert.getEnrollment().getCourse() != null)
                .filter(cert -> {
                    LocalDate expiryDate = cert.getExpiryDate().toLocalDate();
                    long durationDays = cert.getEnrollment().getCourse().getDurationDays();

                    LocalDate courseDurationMatchDate = expiryDate.minusDays(durationDays);
                    LocalDate dayBeforeExpiry = expiryDate.minusDays(1);

                    return today.isEqual(courseDurationMatchDate) || today.isEqual(dayBeforeExpiry);
                })
                .collect(Collectors.toList());

        if (certificatesToProcess.isEmpty()) {
            logger.info("No certificates matched the notification days today.");
            return;
        }

        logger.info("Retrieved {} items requiring reminders. Grouping entries by employee profile...", certificatesToProcess.size());

        Map<Employee, List<Certification>> certificatesByEmployee = certificatesToProcess.stream()
                .collect(Collectors.groupingBy(Certification::getEmployee));

        int successEmailCount = 0;
        int failureEmailCount = 0;

        for (Map.Entry<Employee, List<Certification>> entry : certificatesByEmployee.entrySet()) {
            Employee employee = entry.getKey();
            List<Certification> employeeCertificates = entry.getValue();

            try {
                emailService.sendConsolidatedExpiryEmail(employee.getEmail(), employee.getFirstName(), employeeCertificates);

                logger.info("Successfully dispatched unified email to: {} (Contains {} certificates)", employee.getEmail(), employeeCertificates.size());
                successEmailCount++;

            } catch (Exception e) {
                failureEmailCount++;
                logger.error("Failed handling email dispatch processing context for Employee: {}. Details: {}", employee.getEmployeeId(), e.getMessage());
            }
        }

        logger.info("Job complete. Summary: Distinct Profiles Notified: {}, Failed Operations: {}", successEmailCount, failureEmailCount);
    }
}