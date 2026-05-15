package com.example.service;

import com.example.entity.CertificationRenewal;
import com.example.repo.CertificationRenewalRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CertificationRenewalService {

    private final CertificationRenewalRepo certificationRenewalRepo;

    public CertificationRenewalService(CertificationRenewalRepo certificationRenewalRepo) {
        this.certificationRenewalRepo = certificationRenewalRepo;
    }

    public List<CertificationRenewal> getAllRenewals() {
        return certificationRenewalRepo.findAll();
    }

    public CertificationRenewal saveRenewal(CertificationRenewal renewal) {
        return certificationRenewalRepo.save(renewal);
    }

    public void deleteRenewal(Long id) {
        certificationRenewalRepo.deleteById(id);
    }

    public CertificationRenewal getRenewalById(Long id) {
        return certificationRenewalRepo.findById(id).orElse(null);
    }
}