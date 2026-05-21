package com.example.repo;

import com.example.entity.Certification;
import com.example.entity.CertificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CertificationRepo extends JpaRepository<Certification, Long> {

    List<Certification> findByEnrollmentEnrollmentId(Long enrollmentId);

    Collection<Certification> findByEmployeeEmployeeIdAndIsActiveTrue(Long employeeId);

    Optional<Certification> findByCertificateNumber(String certNo);

    @Query("SELECT c FROM Certification c " +
            "JOIN FETCH c.employee " +
            "JOIN FETCH c.course " +
            "WHERE c.isActive = true " +
            "AND (c.expiryDate <= :now OR c.status = :expiredStatus)")
    List<Certification> findCertificatesToProcess(
            @Param("now") LocalDateTime now,
            @Param("expiredStatus") CertificationStatus expiredStatus
    );
}
