package com.example.repo;

import com.example.entity.CertificationRenewal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CertificationRenewalRepo extends JpaRepository<CertificationRenewal, UUID> {
    
}
