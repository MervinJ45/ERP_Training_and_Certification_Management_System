package com.example.repo;

import com.example.entity.CertificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CertificationStatusRepo  extends JpaRepository<CertificationStatus, Long> {
    
}
