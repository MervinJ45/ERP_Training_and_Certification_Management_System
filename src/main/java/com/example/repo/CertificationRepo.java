package com.example.repo;

import com.example.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface CertificationRepo extends JpaRepository<Certification, Long> {

    Collection<Certification> findByEmployeeEmployeeIdAndIsActiveTrue(Long employeeId);

    Optional<Certification> findByCertificateNumber(String certNo);
}
