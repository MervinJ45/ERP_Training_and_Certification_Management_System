package com.example.service;

import com.example.entity.Certification;
import com.example.repo.CertificationRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CertificationService {

    private final CertificationRepo certificationRepo;

    public CertificationService(
            CertificationRepo certificationRepo
    ) {
        this.certificationRepo = certificationRepo;
    }

    public List<Certification> getAllCertifications() {
        return certificationRepo.findAll();
    }

    public Certification saveCertification(
            Certification certification
    ) {
        return certificationRepo.save(certification);
    }

    public void deleteCertification(UUID id) {
        certificationRepo.deleteById(id);
    }

    public Certification getCertificationById(UUID id) {
        return certificationRepo.findById(id).orElse(null);
    }
}