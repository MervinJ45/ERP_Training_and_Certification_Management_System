package com.example.repo;

import com.example.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CertificationRepo extends JpaRepository<Certification, UUID> {
    
}
